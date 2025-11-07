package uk.gov.moj.cpp.defence.query.api.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.services.core.requester.Requester;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class CalendarServiceTest {

    @Mock
    private RefDataService referenceDataService;

    @Mock
    private Requester requester;

    @InjectMocks
    private CalendarService calendarService;

    @BeforeEach
    public void setUp() {
        when(referenceDataService.getPublicHolidays(any(), any(), any(), any())).thenReturn(getPublicHolidays());
    }


    @Test
    public void shouldReturnDaysBetweenWhenThereIsNoWeekend() {
        final long result = calendarService.daysBetweenExcludeHolidays(LocalDate.parse("2024-02-05"), LocalDate.parse("2024-02-09"), requester);
        assertThat(result, is(4L));
    }

    @Test
    public void shouldReturnDaysBetweenWhenThereIs2DaysWeekendAndNoBackHoliday() {
        final long result = calendarService.daysBetweenExcludeHolidays(LocalDate.parse("2024-02-05"), LocalDate.parse("2024-02-12"), requester);
        assertThat(result, is(5L));
    }

    @Test
    public void shouldReturnDaysBetweenWhenThereIs2DaysWeekendAnd2DaysBackHoliday() {
        final long result = calendarService.daysBetweenExcludeHolidays(LocalDate.parse("2024-02-05"), LocalDate.parse("2024-02-15"), requester);
        assertThat(result, is(6L));
    }

    @Test
    public void shouldReturnDaysBetweenWhenTwoDatesAreEqual() {
        final long result = calendarService.daysBetweenExcludeHolidays(LocalDate.parse("2024-02-05"), LocalDate.parse("2024-02-05"), requester);
        assertThat(result, is(0L));
    }


    private List<LocalDate> getPublicHolidays() {
        final List<LocalDate> publicHolidays = new ArrayList<>();
        publicHolidays.add(LocalDate.parse("2024-02-13"));
        publicHolidays.add(LocalDate.parse("2024-02-14"));
        return publicHolidays;
    }

}