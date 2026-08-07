package se.eelde.toggles.agent

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBinder

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class CallerAuthorizationTest {

    @Test
    fun `shell uid is authorized`() {
        ShadowBinder.setCallingUid(SHELL_UID)

        assertTrue(CallerAuthorization().isAuthorizedCaller())
    }

    @Test
    fun `root uid is authorized`() {
        ShadowBinder.setCallingUid(ROOT_UID)

        assertTrue(CallerAuthorization().isAuthorizedCaller())
    }

    @Test
    fun `an ordinary application uid is not authorized`() {
        ShadowBinder.setCallingUid(APP_UID)

        assertFalse(CallerAuthorization().isAuthorizedCaller())
    }

    @Test
    fun `the system uid is not authorized`() {
        ShadowBinder.setCallingUid(SYSTEM_UID)

        assertFalse(CallerAuthorization().isAuthorizedCaller())
    }

    @Test
    fun `another application uid in the normal app range is not authorized`() {
        ShadowBinder.setCallingUid(SECOND_APP_UID)

        assertFalse(CallerAuthorization().isAuthorizedCaller())
    }

    private companion object {
        const val ROOT_UID = 0
        const val SYSTEM_UID = 1000
        const val SHELL_UID = 2000
        const val APP_UID = 10247
        const val SECOND_APP_UID = 10001
    }
}
