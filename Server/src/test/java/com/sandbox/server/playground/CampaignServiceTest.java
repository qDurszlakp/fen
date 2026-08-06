package com.sandbox.server.playground;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CampaignServiceTest {

    private static CampaignService service;

    @BeforeAll
    static void setUp() {
        service = new CampaignService();
    }

    private static Campaign campaign(String code, LocalDate validFrom, LocalDate validTo, int priority) {
        return new Campaign(UUID.randomUUID(), code, validFrom, validTo, priority);
    }

    private static Campaign campaign(String code, String validFrom, String validTo, int priority) {
        return campaign(code, LocalDate.parse(validFrom), LocalDate.parse(validTo), priority);
    }

    @Nested
    @DisplayName("activeCampaignsAt")
    class ActiveCampaignsAt {

        @Test
        void shouldReturnEmptyListForNullInput() {
            // when
            List<Campaign> result = service.activeCampaignsAt(null, LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyListForEmptyInput() {
            // when
            List<Campaign> result = service.activeCampaignsAt(List.of(), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnCampaignWhenDateIsWithinRange() {
            // given
            Campaign spring = campaign("SPRING", "2026-03-01", "2026-03-31", 1);

            // when
            List<Campaign> result = service.activeCampaignsAt(List.of(spring), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).containsExactly(spring);
        }

        @Test
        void shouldIncludeCampaignOnItsFirstDay() {
            // given
            Campaign spring = campaign("SPRING", "2026-03-01", "2026-03-31", 1);

            // when
            List<Campaign> result = service.activeCampaignsAt(List.of(spring), LocalDate.of(2026, 3, 1));

            // then
            assertThat(result).containsExactly(spring);
        }

        @Test
        void shouldIncludeCampaignOnItsLastDay() {
            // given
            Campaign spring = campaign("SPRING", "2026-03-01", "2026-03-31", 1);

            // when
            List<Campaign> result = service.activeCampaignsAt(List.of(spring), LocalDate.of(2026, 3, 31));

            // then
            assertThat(result).containsExactly(spring);
        }

        @Test
        void shouldExcludeCampaignOneDayBeforeItStarts() {
            // given
            Campaign spring = campaign("SPRING", "2026-03-01", "2026-03-31", 1);

            // when
            List<Campaign> result = service.activeCampaignsAt(List.of(spring), LocalDate.of(2026, 2, 28));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldExcludeCampaignOneDayAfterItEnds() {
            // given
            Campaign spring = campaign("SPRING", "2026-03-01", "2026-03-31", 1);

            // when
            List<Campaign> result = service.activeCampaignsAt(List.of(spring), LocalDate.of(2026, 4, 1));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyCampaignsActiveAtGivenDate() {
            // given
            Campaign expired = campaign("EXPIRED", "2026-01-01", "2026-01-31", 1);
            Campaign active = campaign("ACTIVE", "2026-03-01", "2026-03-31", 2);
            Campaign overlapping = campaign("OVERLAPPING", "2026-02-15", "2026-06-30", 3);
            Campaign future = campaign("FUTURE", "2026-12-01", "2026-12-24", 4);

            // when
            List<Campaign> result = service.activeCampaignsAt(
                    List.of(expired, active, overlapping, future), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).containsExactly(active, overlapping);
        }

        @Test
        void shouldIncludeSingleDayCampaignOnThatDay() {
            // given
            Campaign blackFriday = campaign("BLACK_FRIDAY", "2026-11-27", "2026-11-27", 9);

            // when
            List<Campaign> result = service.activeCampaignsAt(List.of(blackFriday), LocalDate.of(2026, 11, 27));

            // then
            assertThat(result).containsExactly(blackFriday);
        }
    }

    @Nested
    @DisplayName("highestPriorityCampaignAt")
    class HighestPriorityCampaignAt {

        @Test
        void shouldReturnEmptyOptionalForNullInput() {
            // when
            Optional<Campaign> result = service.highestPriorityCampaignAt(null, LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyOptionalForEmptyInput() {
            // when
            Optional<Campaign> result = service.highestPriorityCampaignAt(List.of(), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnTheOnlyCampaign() {
            // given
            Campaign single = campaign("SINGLE", "2026-03-01", "2026-03-31", 5);

            // when
            Optional<Campaign> result = service.highestPriorityCampaignAt(List.of(single), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).contains(single);
        }

        @Test
        void shouldReturnCampaignWithHighestPriority() {
            // given
            Campaign low = campaign("LOW", "2026-03-01", "2026-03-31", 1);
            Campaign high = campaign("HIGH", "2026-03-01", "2026-03-31", 10);
            Campaign medium = campaign("MEDIUM", "2026-03-01", "2026-03-31", 5);

            // when
            Optional<Campaign> result = service.highestPriorityCampaignAt(
                    List.of(low, high, medium), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).contains(high);
        }

        @Test
        void shouldReturnFirstCampaignWhenPrioritiesAreTied() {
            // given
            Campaign first = campaign("FIRST", "2026-03-01", "2026-03-31", 7);
            Campaign second = campaign("SECOND", "2026-03-01", "2026-03-31", 7);

            // when
            Optional<Campaign> result = service.highestPriorityCampaignAt(
                    List.of(first, second), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).contains(first);
        }

        @Test
        void shouldReturnCampaignWithPriorityZero() {
            // given
            Campaign zero = campaign("ZERO", "2026-03-01", "2026-03-31", 0);

            // when
            Optional<Campaign> result = service.highestPriorityCampaignAt(List.of(zero), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).contains(zero);
        }

        @Test
        void shouldIgnoreCampaignsNotActiveAtGivenDate() {
            // given
            Campaign expiredButImportant = campaign("EXPIRED", "2026-01-01", "2026-01-31", 10);
            Campaign activeButMinor = campaign("ACTIVE", "2026-03-01", "2026-03-31", 1);

            // when
            Optional<Campaign> result = service.highestPriorityCampaignAt(
                    List.of(expiredButImportant, activeButMinor), LocalDate.of(2026, 3, 15));

            // then
            assertThat(result).contains(activeButMinor);
        }
    }

    @Nested
    @DisplayName("countCampaignsByStartMonth")
    class CountCampaignsByStartMonth {

        @Test
        void shouldReturnEmptyMapForNullInput() {
            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonth(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyMapForEmptyInput() {
            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonth(List.of());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldCountSingleCampaign() {
            // given
            Campaign spring = campaign("SPRING", "2026-03-01", "2026-03-31", 1);

            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonth(List.of(spring));

            // then
            assertThat(result).containsExactly(Map.entry(YearMonth.of(2026, 3), 1L));
        }

        @Test
        void shouldGroupCampaignsByTheMonthTheyStart() {
            // given
            Campaign march = campaign("MARCH", "2026-03-01", "2026-03-31", 1);
            Campaign april = campaign("APRIL", "2026-04-10", "2026-04-20", 2);

            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonth(List.of(march, april));

            // then
            assertThat(result).containsOnly(
                    Map.entry(YearMonth.of(2026, 3), 1L),
                    Map.entry(YearMonth.of(2026, 4), 1L));
        }

        @Test
        void shouldSeparateTheSameMonthOfDifferentYears() {
            // given
            Campaign y2025 = campaign("Y2025", "2025-03-01", "2025-03-31", 1);
            Campaign y2026 = campaign("Y2026", "2026-03-01", "2026-03-31", 2);

            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonth(List.of(y2025, y2026));

            // then
            assertThat(result).containsOnly(
                    Map.entry(YearMonth.of(2025, 3), 1L),
                    Map.entry(YearMonth.of(2026, 3), 1L));
        }

        @Test
        void shouldCountMultipleCampaignsStartingInTheSameMonth() {
            // given
            Campaign first = campaign("FIRST", "2026-03-01", "2026-03-10", 1);
            Campaign second = campaign("SECOND", "2026-03-05", "2026-03-20", 2);
            Campaign third = campaign("THIRD", "2026-03-25", "2026-04-05", 3);

            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonth(List.of(first, second, third));

            // then
            assertThat(result).containsExactly(Map.entry(YearMonth.of(2026, 3), 3L));
        }

        @Test
        void shouldProduceTheSameResultWithoutStreams() {
            // given
            List<Campaign> campaigns = List.of(
                    campaign("FIRST", "2026-03-01", "2026-03-10", 1),
                    campaign("SECOND", "2026-03-05", "2026-03-20", 2),
                    campaign("THIRD", "2026-04-25", "2026-05-05", 3),
                    campaign("FOURTH", "2025-03-01", "2025-03-31", 4));

            // when
            Map<YearMonth, Long> streamed = service.countCampaignsByStartMonth(campaigns);
            Map<YearMonth, Long> looped = service.countCampaignsByStartMonthNoStreams(campaigns);

            // then
            assertThat(looped).isEqualTo(streamed).containsExactly(
                    Map.entry(YearMonth.of(2025, 3), 1L),
                    Map.entry(YearMonth.of(2026, 3), 2L),
                    Map.entry(YearMonth.of(2026, 4), 1L));
        }

        @Test
        void shouldReturnEmptyMapForNullInputWithoutStreams() {
            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonthNoStreams(null);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void shouldGroupByStartMonthNotByEndMonth() {
            // given
            Campaign spanning = campaign("SPANNING", "2026-03-28", "2026-05-02", 1);

            // when
            Map<YearMonth, Long> result = service.countCampaignsByStartMonth(List.of(spanning));

            // then
            assertThat(result).containsExactly(Map.entry(YearMonth.of(2026, 3), 1L));
        }
    }
}
