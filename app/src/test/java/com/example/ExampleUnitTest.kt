package com.example

import com.example.model.EnvironmentType
import com.example.model.PoseCatalog
import com.example.model.PoseSilhouetteType
import com.example.model.SceneCategory
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun poseCatalog_containsGraduationAndBenchPoses() {
    val poses = PoseCatalog.ALL_POSES
    assertTrue(poses.isNotEmpty())

    // Check Graduation pose matching reference image 1
    val gradPose = poses.firstOrNull { it.silhouetteType == PoseSilhouetteType.GRADUATION_KICK_DIPLOMA }
    assertNotNull("Graduation kick pose should exist", gradPose)
    assertEquals(SceneCategory.GRADUATION, gradPose?.category)
    assertEquals(EnvironmentType.OUTDOOR, gradPose?.environment)
    assertTrue("Graduation pose should have annotations", gradPose?.annotations?.isNotEmpty() == true)

    // Check Garden Bench pose matching reference image 2
    val benchPose = poses.firstOrNull { it.silhouetteType == PoseSilhouetteType.BENCH_SITTING_CROSSED }
    assertNotNull("Bench sitting crossed pose should exist", benchPose)
    assertEquals(SceneCategory.GARDEN_BENCH, benchPose?.category)
    assertEquals(EnvironmentType.OUTDOOR, benchPose?.environment)
    assertTrue("Bench pose should have annotations", benchPose?.annotations?.isNotEmpty() == true)
  }

  @Test
  fun sceneCategories_havePoses() {
    SceneCategory.values().forEach { category ->
      val categoryPoses = PoseCatalog.ALL_POSES.filter { it.category == category }
      assertTrue("Category $category should have recommended poses", categoryPoses.isNotEmpty())
    }
  }

  @Test
  fun poseCatalog_hasIndoorAndOutdoorPoses() {
    val indoorPoses = PoseCatalog.INDOOR_POSES
    val outdoorPoses = PoseCatalog.OUTDOOR_POSES

    assertTrue("Should have extensive indoor poses", indoorPoses.size >= 8)
    assertTrue("Should have extensive outdoor poses", outdoorPoses.size >= 8)

    indoorPoses.forEach {
      assertEquals("Indoor pose must have INDOOR environment", EnvironmentType.INDOOR, it.environment)
    }

    outdoorPoses.forEach {
      assertEquals("Outdoor pose must have OUTDOOR environment", EnvironmentType.OUTDOOR, it.environment)
    }
  }

  @Test
  fun poseCatalog_allSilhouetteTypesAreCovered() {
    val catalogSilhouetteTypes = PoseCatalog.ALL_POSES.map { it.silhouetteType }.toSet()
    PoseSilhouetteType.values().forEach { type ->
      assertTrue("PoseSilhouetteType $type should be present in PoseCatalog", catalogSilhouetteTypes.contains(type))
    }
  }
}
