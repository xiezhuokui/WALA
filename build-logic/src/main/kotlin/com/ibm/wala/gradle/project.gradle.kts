package com.ibm.wala.gradle

import com.diffplug.spotless.LineEnding.PLATFORM_NATIVE
import org.gradle.plugins.ide.eclipse.model.AbstractClasspathEntry
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.EclipseModel

// Build configuration shared by all projects *including* the root project.

plugins {
  eclipse
  id("com.diffplug.spotless")
}

repositories.mavenCentral()

////////////////////////////////////////////////////////////////////////
//
//  Eclipse IDE integration
//

// workaround for <https://github.com/gradle/gradle/issues/4802>
the<EclipseModel>().classpath.file.whenMerged {
  (this as Classpath).run {
    entries.forEach {
      if (it is AbstractClasspathEntry && it.entryAttributes["gradle_used_by_scope"] == "test")
          it.entryAttributes["test"] = true
    }
  }
}

////////////////////////////////////////////////////////////////////////
//
//  Helpers for dependency locking
//

// this task resolves dependencies in all sub-projects, making it easy to
// generate lockfiles
tasks.register<DependencyReportTask>("allDeps") {}

////////////////////////////////////////////////////////////////////////
//
//  Code formatting
//

spotless {
  // Workaround for <https://github.com/diffplug/spotless/issues/1644>
  // using idea found at
  // <https://github.com/diffplug/spotless/issues/1527#issuecomment-1409142798>.
  lineEndings = PLATFORM_NATIVE

  findProperty("spotless.ratchet.from")?.let { ratchetFrom(it as String) }

  kotlinGradle {
    ktfmt(
        rootProject
            .the<VersionCatalogsExtension>()
            .named("libs")
            .findVersion("ktfmt")
            .get()
            .toString())
  }
}
