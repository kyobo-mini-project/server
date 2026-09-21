package com.kyobo.server;

import com.kyobo.server.config.FieldEncryptor;
import com.kyobo.server.controller.ConsoleController;
import com.kyobo.server.controller.AdminController;
import com.kyobo.server.controller.UserController;
import com.kyobo.server.entity.User;
import com.kyobo.server.mapper.BookingMapper;
import com.kyobo.server.mapper.UserMapper;
import com.kyobo.server.service.UserService;
import com.kyobo.server.service.UserService.WithdrawResult;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class UserWithdrawTest {
    private static final String PASSWORD = "password123";
    private static final String HASH = FieldEncryptor.hashPassword(PASSWORD);

    @Test void commitsOnlySuccessfulSoftDelete() {
        Fixture f = new Fixture();
        assertEquals(WithdrawResult.SUCCESS, f.service.withdraw(1, PASSWORD));
        assertEquals(List.of("update", "commit", "close"), f.events);
    }

    @Test void activeBookingBlocksBeforePasswordInput() {
        Fixture f = new Fixture();
        f.activeBookings = 1;
        assertFalse(new UserController(new Scanner(""), f.service).runWithdraw(f.user));
        assertEquals(List.of("rollback", "close"), f.events);
    }

    @Test void rejectsWrongAndBlankPasswords() {
        for (String password : List.of("wrong", "", "   ")) {
            Fixture f = new Fixture();
            assertEquals(password.isBlank() ? WithdrawResult.PASSWORD_REQUIRED : WithdrawResult.PASSWORD_MISMATCH,
                    f.service.withdraw(1, password));
            assertEquals(List.of("rollback", "close"), f.events);
        }
    }

    @Test void rejectsDeletedAndMissingUsers() {
        Fixture deleted = new Fixture();
        deleted.user.setIsDeleted(true);
        assertEquals(WithdrawResult.INVALID_USER, deleted.service.withdraw(1, PASSWORD));
        assertEquals(List.of("rollback", "close"), deleted.events);
        Fixture missing = new Fixture();
        missing.user = null;
        assertEquals(WithdrawResult.INVALID_USER, missing.service.withdraw(1, PASSWORD));
        assertEquals(List.of("rollback", "close"), missing.events);
    }

    @Test void zeroUpdatedRowsRollsBack() {
        Fixture f = new Fixture();
        f.updatedRows = 0;
        assertEquals(WithdrawResult.INVALID_USER, f.service.withdraw(1, PASSWORD));
        assertEquals(List.of("update", "rollback", "close"), f.events);
    }

    @Test void queryFailureRollsBackAndCloses() {
        Fixture f = new Fixture();
        f.failUpdate = true;
        assertThrows(PersistenceException.class, () -> f.service.withdraw(1, PASSWORD));
        assertEquals(List.of("update", "rollback", "close"), f.events);
    }

    @Test void retriesOnlyPasswordThenWithdraws() {
        Fixture f = new Fixture();
        assertTrue(new UserController(new Scanner("\nwrong\n" + PASSWORD + "\n0\n"), f.service)
                .runWithdraw(f.user));
        assertEquals(1, f.events.stream().filter("update"::equals).count());
        assertTrue(f.events.contains("commit"));
    }

    @Test void cancellationAndGuestNeverUpdate() {
        Fixture f = new Fixture();
        assertFalse(new UserController(new Scanner(PASSWORD + "\n1\n"), f.service).runWithdraw(f.user));
        assertFalse(f.events.contains("update"));
        f.events.clear();
        assertFalse(new UserController(new Scanner(""), f.service).runWithdraw(null));
        assertTrue(f.events.isEmpty());
    }

    @Test void rechecksBookingsAtFinalConfirmation() {
        Fixture f = new Fixture();
        f.blockOnThirdCheck = true;
        assertFalse(new UserController(new Scanner(PASSWORD + "\n0\n"), f.service).runWithdraw(f.user));
        assertFalse(f.events.contains("update"));
    }

    @Test void databaseFailureKeepsSession() {
        Fixture f = new Fixture();
        f.failUpdate = true;
        assertFalse(new UserController(new Scanner(PASSWORD + "\n0\n"), f.service).runWithdraw(f.user));
        assertFalse(f.events.contains("commit"));
    }

    @Test void successfulAndAlreadyDeletedUsersReturnToGuestHome() throws Exception {
        for (boolean deleted : List.of(false, true)) {
            Fixture f = new Fixture();
            f.user.setIsDeleted(deleted);
            Scanner scanner = new Scanner("2\nid\npw\n4\n" + (deleted ? "" : PASSWORD + "\n0\n") + "0\n0\n");
            UserService service = new UserService(f.factory) {
                @Override public User signIn(String loginId, String password) { return f.user; }
            };
            AdminController admin = new AdminController(scanner) {
                @Override public boolean tryEnterAdminMode(String input) { return false; }
            };
            ConsoleController console = new ConsoleController(scanner,
                    new UserController(scanner, service), null, admin);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            PrintStream original = System.out;
            try {
                System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
                console.run();
            } finally {
                System.setOut(original);
            }
            String text = output.toString(StandardCharsets.UTF_8);
            String message = deleted ? "유효하지 않은 회원정보입니다." : "회원 탈퇴가 완료되었습니다.";
            assertTrue(text.indexOf("[2. 로그인]", text.indexOf(message)) > text.indexOf(message));
            var currentUser = ConsoleController.class.getDeclaredField("currentUser");
            currentUser.setAccessible(true);
            assertNull(currentUser.get(console));
        }
    }

    @Test void myBatisLoadsWithdrawalStatements() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("db.url", "jdbc:postgresql://localhost/test");
        properties.setProperty("db.username", "test");
        properties.setProperty("db.password", "test");
        try (var input = Resources.getResourceAsStream("mybatis-config.xml")) {
            var config = new SqlSessionFactoryBuilder().build(input, properties).getConfiguration();
            assertTrue(config.hasStatement(UserMapper.class.getName() + ".softDelete"));
            assertTrue(config.hasStatement(UserMapper.class.getName() + ".findById"));
            assertTrue(config.hasStatement(BookingMapper.class.getName() + ".findActiveBookingsByUserId"));
        }
    }

    private static class Fixture {
        User user = new User();
        int activeBookings;
        int updatedRows = 1;
        int bookingChecks;
        boolean failUpdate;
        boolean blockOnThirdCheck;
        final List<String> events = new ArrayList<>();
        final SqlSessionFactory factory;
        final UserService service;

        Fixture() {
            user.setUserId(1);
            user.setName("테스트");
            user.setUserPw(HASH);
            user.setIsDeleted(false);
            UserMapper users = proxy(UserMapper.class, (p, method, args) -> {
                if (method.getName().equals("findById")) return user;
                if (method.getName().equals("softDelete")) {
                    events.add("update");
                    if (failUpdate) throw new PersistenceException("test database error");
                    return updatedRows;
                }
                throw new AssertionError(method.getName());
            });
            BookingMapper bookings = proxy(BookingMapper.class, (p, method, args) -> {
                if (method.getName().equals("findActiveBookingsByUserId")) {
                    bookingChecks++;
                    return blockOnThirdCheck && bookingChecks >= 3 ? 1 : activeBookings;
                }
                throw new AssertionError(method.getName());
            });
            SqlSession session = proxy(SqlSession.class, (p, method, args) -> {
                if (method.getName().equals("getMapper")) return args[0] == UserMapper.class ? users : bookings;
                if (List.of("commit", "rollback", "close").contains(method.getName())) {
                    events.add(method.getName());
                    return null;
                }
                throw new AssertionError(method.getName());
            });
            factory = proxy(SqlSessionFactory.class, (p, method, args) -> {
                assertEquals("openSession", method.getName());
                assertArrayEquals(new Object[]{false}, args);
                return session;
            });
            service = new UserService(factory);
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
