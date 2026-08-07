package se.eelde.toggles.provider

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
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

    @Test
    fun `applicationLabel resolves the real label for an installed package`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val packageName = context.packageName
        // Binder.getCallingUid() defaults to the test process's own uid, which Robolectric
        // already registers as the test package's uid — no setNameForUid/setCallingUid needed.

        // Give the test package an explicit label so this test can tell a genuinely resolved
        // label apart from the package-name fallback — Robolectric's default label may otherwise
        // equal the package name, which would make the assertion vacuous.
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        packageInfo.applicationInfo?.nonLocalizedLabel = "Real Application Label"
        shadowOf(context.packageManager).installPackage(packageInfo)

        val wrapper = PackageManagerWrapper(context.packageManager)

        assertEquals("Real Application Label", wrapper.applicationLabel)
        assertNotEquals(packageName, wrapper.applicationLabel)
    }

    @Test
    fun `callingApplicationPackageName rejects a caller whose uid cannot be resolved`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        // Deliberately no setNameForUid() for this uid, so getNameForUid() returns null — what
        // the platform documents for a uid that "is not currently assigned".
        ShadowBinder.setCallingUid(UNASSIGNED_UID)

        val wrapper = PackageManagerWrapper(context.packageManager)

        val exception = assertThrows(SecurityException::class.java) {
            wrapper.callingApplicationPackageName
        }
        assertTrue(
            "Message should name the offending uid, was: ${exception.message}",
            exception.message.orEmpty().contains(UNASSIGNED_UID.toString())
        )
    }

    @Test
    fun `applicationLabel rejects a caller whose uid cannot be resolved`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        ShadowBinder.setCallingUid(UNASSIGNED_UID)

        val wrapper = PackageManagerWrapper(context.packageManager)

        // The package-name fallback must not paper over an unidentifiable caller.
        assertThrows(SecurityException::class.java) { wrapper.applicationLabel }
    }

    private companion object {
        /** A uid Robolectric has no package name registered for. */
        const val UNASSIGNED_UID = 99999
    }
}
