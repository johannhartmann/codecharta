package de.maibornwolff.codecharta.analysers.parsers.unified.metriccollectors

import de.maibornwolff.treesitter.excavationsite.api.AvailableFileMetrics
import de.maibornwolff.treesitter.excavationsite.api.Language
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class ScalaCollectorTest {
    private val collector = TreeSitterLibraryCollector(Language.SCALA)

    private fun createTestFile(content: String): File {
        val tempFile = File.createTempFile("testFile", ".scala")
        tempFile.writeText(content)
        tempFile.deleteOnExit()
        return tempFile
    }

    private fun assertMetric(content: String, metric: String, expectedValue: Double) {
        val result = collector.collectMetricsForFile(createTestFile(content))
        Assertions.assertThat(result.attributes[metric]).isEqualTo(expectedValue)
    }

    @Test
    fun `should count Scala 2 function and if expression for complexity`() {
        val content = """
            object Sample {
              def sign(x: Int): Int = {
                if (x > 0) 1 else -1
              }
            }
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.NUMBER_OF_FUNCTIONS.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.LOGIC_COMPLEXITY.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.COMPLEXITY.metricName, 2.0)
    }

    @Test
    fun `should count Scala 3 braceless function and if expression for complexity`() {
        val content = """
            object Sample:
              def sign(x: Int): Int =
                if x > 0 then 1 else -1
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.NUMBER_OF_FUNCTIONS.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.LOGIC_COMPLEXITY.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.COMPLEXITY.metricName, 2.0)
    }

    @Test
    fun `should count match cases and guards for complexity`() {
        val content = """
            def classify(x: Int): String =
              x match
                case 0 => "zero"
                case n if n > 0 => "positive"
                case _ => "negative"
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.LOGIC_COMPLEXITY.metricName, 5.0)
        assertMetric(content, AvailableFileMetrics.COMPLEXITY.metricName, 6.0)
    }

    @Test
    fun `should count curried and using parameters`() {
        val content = """
            def render(a: Int)(b: String)(using ctx: Context): String =
              s"${'$'}a-${'$'}b-${'$'}ctx"
        """.trimIndent()

        assertMetric(content, "max_parameters_per_function", 3.0)
    }

    @Test
    fun `should count extension methods as functions`() {
        val content = """
            extension (value: String)
              def doubled: String = value + value
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.NUMBER_OF_FUNCTIONS.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.COMPLEXITY.metricName, 1.0)
    }

    @Test
    fun `should count given definitions with parameters as functions`() {
        val content = """
            given ordering(using config: Config, logger: Logger): Ordering[User] with
              def compare(left: User, right: User): Int = left.id.compare(right.id)
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.NUMBER_OF_FUNCTIONS.metricName, 1.0)
        assertMetric(content, "max_parameters_per_function", 2.0)
    }

    @Test
    fun `should count enum methods as functions`() {
        val content = """
            enum Direction:
              case North, South

              def label: String =
                this.toString
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.NUMBER_OF_FUNCTIONS.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.COMPLEXITY.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.REAL_LINES_OF_CODE.metricName, 4.0)
    }

    @Test
    fun `should count for comprehension as logic complexity`() {
        val content = """
            def pairs(xs: List[Int], ys: List[Int]): List[(Int, Int)] =
              for
                x <- xs
                y <- ys if x < y
              yield (x, y)
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.NUMBER_OF_FUNCTIONS.metricName, 1.0)
        assertMetric(content, AvailableFileMetrics.LOGIC_COMPLEXITY.metricName, 2.0)
        assertMetric(content, AvailableFileMetrics.COMPLEXITY.metricName, 3.0)
    }

    @Test
    fun `should count comments without string literals for comment lines`() {
        val content = """
            // line comment
            /*
             * block comment
             */
            val text = "not a comment"
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.COMMENT_LINES.metricName, 4.0)
    }

    @Test
    fun `should count chained Scala calls as message chains`() {
        val content = """
            val rendered = user.a().profile.b().c().d()
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.MESSAGE_CHAINS.metricName, 1.0)
    }

    @Test
    fun `should not count property-only Scala chains as message chains`() {
        val content = """
            val name = user.profile.address.street.name
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.MESSAGE_CHAINS.metricName, 0.0)
    }

    @Test
    fun `should count logical operators in infix expressions`() {
        val content = """
            def active(user: User): Boolean =
              if user.enabled && user.visible || user.owner then true else false
        """.trimIndent()

        assertMetric(content, AvailableFileMetrics.LOGIC_COMPLEXITY.metricName, 3.0)
        assertMetric(content, AvailableFileMetrics.COMPLEXITY.metricName, 4.0)
    }
}
