package com.sandbox.server.playground;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class CampaignService {

    public List<Campaign> activeCampaignsAt(List<Campaign> campaigns, LocalDate date) {

        if (campaigns == null) {
            return List.of();
        }

        return campaigns.stream()
                .filter(c -> !date.isBefore(c.validFrom()) && !date.isAfter(c.validTo()))
                .toList();
    }

    public Optional<Campaign> highestPriorityCampaignAt(List<Campaign> campaigns, LocalDate date) {
        return activeCampaignsAt(campaigns, date)
                .stream()
                .max(Comparator.comparingInt(Campaign::priority));
    }

    public Map<YearMonth, Long> countCampaignsByStartMonth(List<Campaign> campaigns) {
        if (campaigns == null) {
            return Map.of();
        }

        return campaigns.stream()
                .collect(Collectors.groupingBy(
                        c -> YearMonth.from(c.validFrom()),
                        TreeMap::new,
                        Collectors.counting())
                );
    }

    public Map<YearMonth, Long> countCampaignsByStartMonthNoStreams(List<Campaign> campaigns) {
        if (campaigns == null) {
            return Map.of();
        }

        Map<YearMonth, Long> result = new TreeMap<>();

        for (Campaign campaign : campaigns) {
            YearMonth startMonth = YearMonth.from(campaign.validFrom());
            result.merge(startMonth, 1L, Long::sum);
        }

        return result;
    }



}
