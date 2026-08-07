package se.eelde.toggles.provider

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBinder

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PackageManagerWrapperTest {

    @Test
    fun `applicationLabel falls back to package name when the package cannot be resolved`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        // uid 2000 resolves to the shared user name "android.uid.shell:2000", which is not an
        // installed package, so getApplicationInfo() throws NameNotFoundException.
        shadowOf(context.packageManager).setNameForUid(2000, "android.uid.shell:2000")
        ShadowBinder.setCallingUid(2000)

        val wrapper = PackageManagerWrapper(context.packageManager)

        assertEquals("android.uid.shell:2000", wrapper.applicationLabel)
    }

    @Test
    fun `callingApplicationPackageName returns the resolved name`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(context.packageManager).setNameForUid(2000, "android.uid.shell:2000")
        ShadowBinder.setCallingUid(2000)

        val wrapper = PackageManagerWrapper(context.packageManager)

        assertEquals("android.uid.shell:2000", wrapper.callingApplicationPackageName)
    }
}
