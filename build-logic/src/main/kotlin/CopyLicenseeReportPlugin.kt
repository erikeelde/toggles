import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.register
import java.util.Locale

class CopyLicenseeReportPlugin : Plugin<Project> {
    override fun apply(project: Project) {

        val extension =
            project.extensions.getByName("androidComponents") as ApplicationAndroidComponentsExtension

        extension.onVariants { variant ->
            val capitalizedVariantName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else {
                    it.toString()
                }
            }

            val copyArtifactsTask =
                project.tasks.register<AssetCopyTask>("copy${capitalizedVariantName}ArtifactList") {
                    inputFile.set(
                        project.extensions.getByType(ReportingExtension::class.java)
                            .file("licensee/${variant.name}/artifacts.json")
                    )
                }

            variant.sources.assets!!.addGeneratedSourceDirectory(
                copyArtifactsTask,
                AssetCopyTask::outputDirectory
            )

            copyArtifactsTask.dependsOn("licensee${capitalizedVariantName}")
        }
    }
}