package com.mustalk.minisimulator.utils

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StyleRes
import androidx.core.util.Preconditions
import androidx.fragment.app.Fragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.mustalk.minisimulator.HiltTestActivity
import com.mustalk.minisimulator.R

/**
 * launchFragmentInContainer from the androidx.fragment:fragment-testing library
 * is NOT possible to use right now as it uses a hardcoded Activity under the hood
 * (i.e. [androidx.fragment.app.testing.EmptyFragmentActivity]) which is not annotated with @AndroidEntryPoint.
 *
 * As a workaround, use this function that is equivalent. It requires you to add
 * [HiltTestActivity] in the debug folder and include it in the debug AndroidManifest.xml file
 * as can be found in this project.
 *
 * @author by MusTalK on 15/07/2024
 */
inline fun <reified T : Fragment> launchFragmentInHiltContainer(
    fragmentArgs: Bundle? = null,
    @StyleRes themeResId: Int = R.style.AppTheme,
    crossinline action: (T) -> Unit = {},
) {
    val startActivityIntent =
        Intent
            .makeMainActivity(
                ComponentName(ApplicationProvider.getApplicationContext(), HiltTestActivity::class.java)
            ).putExtra("androidx.fragment.app.testing.FragmentScenario.EmptyFragmentActivity.THEME_EXTRAS_BUNDLE_KEY", themeResId)

    ActivityScenario.launch<HiltTestActivity>(startActivityIntent).onActivity { activity ->
        val fragment: Fragment =
            activity.supportFragmentManager.fragmentFactory.instantiate(
                Preconditions.checkNotNull(T::class.java.classLoader),
                T::class.java.name
            )

        fragment.arguments = fragmentArgs
        activity.supportFragmentManager
            .beginTransaction()
            .add(android.R.id.content, fragment, "")
            .commitNow()

        action(fragment as T)
    }
}
