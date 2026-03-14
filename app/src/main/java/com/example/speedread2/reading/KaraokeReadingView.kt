package com.example.speedread2.reading

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.speedread2.R

/**
 * Continuous karaoke text renderer for reading flows.
 * Keeps neighboring lines visible and smoothly follows active token.
 */
class KaraokeReadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ScrollView(context, attrs) {

    private data class LineUi(
        val lineIndex: Int,
        val startToken: Int,
        val endToken: Int,
        val tokens: List<String>,
        val view: TextView
    )

    private val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(6), dp(6), dp(6), dp(6))
    }

    private val lines = mutableListOf<LineUi>()

    private val colorRead = ContextCompat.getColor(context, R.color.primary)
    private val colorActive = ContextCompat.getColor(context, R.color.primary_dark)
    private val colorAhead = ContextCompat.getColor(context, R.color.text_primary)
    private val colorDim = ContextCompat.getColor(context, R.color.text_tertiary)

    init {
        isFillViewport = true
        overScrollMode = OVER_SCROLL_NEVER
        addView(container, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun setTokens(tokens: List<TokenUnit>) {
        container.removeAllViews()
        lines.clear()
        if (tokens.isEmpty()) return

        val grouped = tokens.groupBy { it.lineIndex }.toSortedMap()
        grouped.forEach { (lineIndex, lineTokens) ->
            val lineWords = lineTokens.sortedBy { it.tokenIndex }
            val start = lineWords.first().tokenIndex
            val end = lineWords.last().tokenIndex

            val tv = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                setLineSpacing(0f, 1.15f)
                setPadding(dp(8), dp(10), dp(8), dp(10))
                setTextColor(colorAhead)
                alpha = 0.95f
                text = lineWords.joinToString(" ") { it.raw }
            }

            container.addView(tv, LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = dp(4)
            })

            lines += LineUi(
                lineIndex = lineIndex,
                startToken = start,
                endToken = end,
                tokens = lineWords.map { it.raw },
                view = tv
            )
        }
    }

    fun render(state: TextAligner.AlignmentState?) {
        if (state == null || lines.isEmpty()) return

        val matched = state.matchedTokenIndices
        val active = state.activeTokenIndex

        lines.forEachIndexed { index, line ->
            val text = SpannableString(line.tokens.joinToString(" "))
            var cursor = 0
            line.tokens.forEachIndexed { tokenOffset, token ->
                val globalIndex = line.startToken + tokenOffset
                val start = cursor
                val end = cursor + token.length
                val color = when {
                    globalIndex in matched -> colorRead
                    globalIndex == active -> colorActive
                    else -> colorAhead
                }
                text.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                cursor = end + 1
            }

            line.view.text = text

            when {
                line.endToken < active -> {
                    line.view.alpha = 0.55f
                    line.view.setTypeface(line.view.typeface, Typeface.NORMAL)
                    line.view.setTextColor(colorDim)
                }
                line.startToken <= active && line.endToken >= active -> {
                    line.view.alpha = 1f
                    line.view.setTypeface(line.view.typeface, Typeface.BOLD)
                }
                else -> {
                    line.view.alpha = if (index <= findLineIndexByToken(active) + 1) 0.95f else 0.82f
                    line.view.setTypeface(line.view.typeface, Typeface.NORMAL)
                }
            }
        }

        smoothFollowActive(active)
    }

    private fun smoothFollowActive(activeTokenIndex: Int) {
        val activeLinePos = findLineIndexByToken(activeTokenIndex)
        if (activeLinePos < 0 || activeLinePos >= lines.size) return

        val targetView = lines[activeLinePos].view
        post {
            val target = (targetView.top - height * 0.35f).toInt().coerceAtLeast(0)
            smoothScrollTo(0, target)
        }
    }

    private fun findLineIndexByToken(tokenIndex: Int): Int {
        return lines.indexOfFirst { tokenIndex in it.startToken..it.endToken }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}
