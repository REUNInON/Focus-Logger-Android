package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.util.MarkdownImporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Focus Logger", appName)
  }

  @Test
  fun `test markdown import parser`() {
    val sampleMarkdown = """
      # Focus Session Log
      
      **Start Time:** 2026-08-30 10:00:00
      **End Time:**   2026-08-30 11:30:00
      
      ## Goals
      - [x] Complete database schema (Completed at 25m 00s)
      - [ ] Implement Pomodoro timer
      
      ## Summary
      - **Total Work:** 01h 15m 00s
      - **Total Break:** 00h 10m 00s
      - **Total Slacking:** 00h 05m 00s
      - **Mind Wanders (Passive):** 3
      - **Focus Efficiency:** 83.3%
      
      ## Timeline
      - [10:00] **Working**: 25m 00s
      - [10:25] **Break**: 5m 00s
      - [10:30] **Working**: 50m 00s
      
      ## Deferred Tasks (Distractions)
      - [x] Check email
      - [ ] Order groceries
    """.trimIndent()

    val result = MarkdownImporter.parseMarkdown(sampleMarkdown)
    assertTrue(result.isSuccess)

    val parsed = result.getOrNull()!!
    assertEquals(4500L, parsed.session.totalWorkSeconds) // 1h 15m
    assertEquals(600L, parsed.session.totalBreakSeconds) // 10m
    assertEquals(300L, parsed.session.totalSlackSeconds) // 5m
    assertEquals(3, parsed.session.distractionCount)
    assertEquals(2, parsed.goals.size)
    assertTrue(parsed.goals[0].isCompleted)
    assertEquals(2, parsed.deferredTasks.size)
    assertEquals(3, parsed.timelineBlocks.size)
  }
}

