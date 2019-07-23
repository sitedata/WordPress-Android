package org.wordpress.android.ui.stats.refresh.lists.detail

import org.wordpress.android.fluxc.model.stats.PostDetailStatsModel
import org.wordpress.android.fluxc.model.stats.PostDetailStatsModel.Year
import org.wordpress.android.fluxc.network.utils.StatsGranularity.DAYS
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem.Divider
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem.ExpandableItem
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem.ListItemWithIcon
import org.wordpress.android.ui.stats.refresh.lists.sections.BlockListItem.ListItemWithIcon.TextStyle.LIGHT
import org.wordpress.android.ui.stats.refresh.utils.ContentDescriptionHelper
import org.wordpress.android.ui.stats.refresh.utils.StatsDateFormatter
import org.wordpress.android.ui.stats.refresh.utils.toFormattedString
import org.wordpress.android.util.LocaleManagerWrapper
import java.text.DateFormatSymbols
import java.util.Date
import javax.inject.Inject

class PostDetailMapper
@Inject constructor(
    private val localeManagerWrapper: LocaleManagerWrapper,
    private val statsDateFormatter: StatsDateFormatter,
    private val contentDescriptionHelper: ContentDescriptionHelper
) {
    fun mapYears(
        shownYears: List<Year>,
        expandedYearUiState: ExpandedYearUiState,
        keyLabel: Int,
        valueLabel: Int,
        onUiState: (ExpandedYearUiState) -> Unit
    ): List<BlockListItem> {
        val yearList = mutableListOf<BlockListItem>()

        shownYears.forEachIndexed { index, year ->
            val isNextNotExpanded = shownYears.getOrNull(index + 1)?.year != expandedYearUiState.expandedYear
            if (year.months.isNotEmpty()) {
                val isExpanded = year.year == expandedYearUiState.expandedYear
                if (isExpanded) {
                    yearList.add(Divider)
                }
                yearList.add(
                        ExpandableItem(
                                mapYear(year, index, shownYears.size, isNextNotExpanded, keyLabel, valueLabel),
                                isExpanded = isExpanded
                        ) { changedExpandedState ->
                            val expandedYear = if (changedExpandedState) year.year else null
                            onUiState(expandedYearUiState.copy(expandedYear = expandedYear))
                        })
                if (isExpanded) {
                    yearList.addAll(year.months.sortedByDescending { it.month }.map { month ->
                        val text = DateFormatSymbols(localeManagerWrapper.getLocale()).shortMonths[month.month - 1]
                        val value = month.count.toFormattedString(locale = localeManagerWrapper.getLocale())
                        ListItemWithIcon(
                                text = text,
                                value = value,
                                textStyle = LIGHT,
                                showDivider = false,
                                contentDescription = contentDescriptionHelper.buildContentDescription(
                                        keyLabel,
                                        text,
                                        valueLabel,
                                        value
                                )
                        )
                    })
                    yearList.add(Divider)
                }
            } else {
                yearList.add(
                        mapYear(year, index, shownYears.size, isNextNotExpanded, keyLabel, valueLabel)
                )
            }
        }
        return yearList
    }

    private fun mapYear(
        year: Year,
        index: Int,
        size: Int,
        isNextNotExpanded: Boolean,
        keyLabel: Int,
        valueLabel: Int
    ): ListItemWithIcon {
        val value = year.value.toFormattedString(locale = localeManagerWrapper.getLocale())
        val text = year.year.toString()
        return ListItemWithIcon(
                text = text,
                value = value,
                showDivider = isNextNotExpanded && index < size - 1,
                contentDescription = contentDescriptionHelper.buildContentDescription(
                        keyLabel,
                        text,
                        valueLabel,
                        value
                )
        )
    }

    fun mapWeeks(
        weeks: List<PostDetailStatsModel.Week>,
        visibleCount: Int,
        uiState: ExpandedWeekUiState,
        keyLabel: Int,
        valueLabel: Int,
        onUiState: (ExpandedWeekUiState) -> Unit
    ): List<BlockListItem> {
        val weekList = mutableListOf<BlockListItem>()
        val visibleWeeks = weeks.map { week ->
            val days = week.days.map { DayUiModel(statsDateFormatter.parseStatsDate(DAYS, it.period), it.count) }
            val firstDay = days.first().date
            val lastDay = if (days.size > 1) days.last().date else null
            val descendingDays = days.sortedByDescending { it.date }
            WeekUiModel(firstDay, lastDay, descendingDays, week.average)
        }.sortedByDescending { it.firstDay }.take(visibleCount)
        visibleWeeks.forEachIndexed { index, week ->
            val isNextNotExpanded = visibleWeeks.getOrNull(index + 1)?.firstDay != uiState.expandedWeekFirstDay
            if (week.days.isNotEmpty()) {
                val isExpanded = week.firstDay == uiState.expandedWeekFirstDay
                if (isExpanded) {
                    weekList.add(Divider)
                }
                weekList.add(
                        ExpandableItem(
                                mapWeek(week, index, visibleWeeks.size, isNextNotExpanded, keyLabel, valueLabel),
                                isExpanded = isExpanded
                        ) { changedExpandedState ->
                            val expandedFirstDay = if (changedExpandedState) week.firstDay else null
                            onUiState(uiState.copy(expandedWeekFirstDay = expandedFirstDay))
                        })
                if (isExpanded) {
                    weekList.addAll(week.days
                            .map { day ->
                                val value = day.average.toFormattedString(locale = localeManagerWrapper.getLocale())
                                val text = statsDateFormatter.printDayWithoutYear(day.date)
                                ListItemWithIcon(
                                        text = text,
                                        value = value,
                                        textStyle = LIGHT,
                                        showDivider = false,
                                        contentDescription = contentDescriptionHelper.buildContentDescription(
                                                keyLabel,
                                                text,
                                                valueLabel,
                                                value
                                        )
                                )
                            })
                    weekList.add(Divider)
                }
            } else {
                weekList.add(mapWeek(week, index, visibleWeeks.size, isNextNotExpanded, keyLabel, valueLabel))
            }
        }
        return weekList
    }

    private data class DayUiModel(val date: Date, val average: Int)

    private data class WeekUiModel(
        val firstDay: Date,
        val lastDay: Date? = null,
        val days: List<DayUiModel>,
        val weekAverage: Int
    )

    private fun mapWeek(
        week: WeekUiModel,
        index: Int,
        size: Int,
        isNextNotExpanded: Boolean,
        keyLabel: Int,
        valueLabel: Int
    ): ListItemWithIcon {
        val lastDay = week.lastDay
        val label = if (lastDay != null) {
            statsDateFormatter.printWeek(week.firstDay, lastDay)
        } else {
            statsDateFormatter.printGranularDate(week.firstDay, DAYS)
        }
        val value = week.weekAverage.toFormattedString(locale = localeManagerWrapper.getLocale())
        return ListItemWithIcon(
                text = label,
                value = value,
                showDivider = isNextNotExpanded && index < size - 1,
                contentDescription = contentDescriptionHelper.buildContentDescription(
                        keyLabel,
                        label,
                        valueLabel,
                        value
                )
        )
    }

    data class ExpandedYearUiState(val expandedYear: Int? = null)
    data class ExpandedWeekUiState(val expandedWeekFirstDay: Date? = null)
}
