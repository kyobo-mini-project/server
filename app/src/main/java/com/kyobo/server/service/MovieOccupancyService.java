package com.kyobo.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.kyobo.server.config.MyBatisConfig;
import com.kyobo.server.entity.MovieOccupancy;
import com.kyobo.server.entity.MovieSeatCount;
import com.kyobo.server.mapper.MovieMapper;

public class MovieOccupancyService {

    /**
     * 영화별 점유율을 계산한다.
     * 점유율(%) = 유효 좌석(티켓) 수 ÷ 판매 가능 좌석 수 × 100
     */
    public List<MovieOccupancy> getAllOccupancies() {
        try (SqlSession session = MyBatisConfig.sqlSessionFactory().openSession()) {
            MovieMapper mapper = session.getMapper(MovieMapper.class);
            Map<Integer, Long> availableByMovie = toCountMap(mapper.findAvailableSeatCounts());
            Map<Integer, Long> soldByMovie = toCountMap(mapper.findSoldSeatCounts());

            List<MovieOccupancy> result = new ArrayList<>();
            for (Map.Entry<Integer, Long> entry : availableByMovie.entrySet()) {
                int movieId = entry.getKey();
                long available = entry.getValue();
                long sold = soldByMovie.getOrDefault(movieId, 0L);

                MovieOccupancy occupancy = new MovieOccupancy();
                occupancy.setMovieId(movieId);
                occupancy.setAvailableSeatCount(available);
                occupancy.setSoldSeatCount(sold);
                occupancy.setOccupancyRate(OccupancyCalculator.calculate(sold, available));
                result.add(occupancy);
            }
            return result;
        }
    }

    public MovieOccupancy getOccupancy(int movieId) {
        for (MovieOccupancy occupancy : getAllOccupancies()) {
            if (occupancy.getMovieId() == movieId) {
                return occupancy;
            }
        }
        MovieOccupancy empty = new MovieOccupancy();
        empty.setMovieId(movieId);
        empty.setSoldSeatCount(0);
        empty.setAvailableSeatCount(0);
        empty.setOccupancyRate(OccupancyCalculator.calculate(0, 0));
        return empty;
    }

    /**
     * 점유율 높은 순 순위 (1부터).
     * 점유율이 같으면 영화 번호와 관계없이 같은 순위로 처리한다. (공동 순위)
     */
    public int getOccupancyRank(int movieId) {
        List<MovieOccupancy> rankings = new ArrayList<>(getAllOccupancies());
        boolean included = false;
        for (MovieOccupancy occupancy : rankings) {
            if (occupancy.getMovieId() == movieId) {
                included = true;
                break;
            }
        }
        if (!included) {
            rankings.add(getOccupancy(movieId));
        }

        rankings.sort((left, right) ->
                Double.compare(right.getOccupancyRate(), left.getOccupancyRate()));

        int currentRank = 1;
        for (int i = 0; i < rankings.size(); i++) {
            if (i > 0
                    && Double.compare(
                            rankings.get(i).getOccupancyRate(),
                            rankings.get(i - 1).getOccupancyRate()) != 0) {
                currentRank = i + 1;
            }
            if (rankings.get(i).getMovieId() == movieId) {
                return currentRank;
            }
        }
        return currentRank;
    }

    private static Map<Integer, Long> toCountMap(List<MovieSeatCount> rows) {
        Map<Integer, Long> map = new HashMap<>();
        if (rows == null) {
            return map;
        }
        for (MovieSeatCount row : rows) {
            map.put(row.getMovieId(), row.getSeatCount());
        }
        return map;
    }
}
