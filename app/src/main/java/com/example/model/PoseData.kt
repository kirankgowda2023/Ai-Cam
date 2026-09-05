package com.example.model

enum class EnvironmentType(val displayName: String, val emoji: String) {
    ALL("All Poses", "✨"),
    INDOOR("Indoor", "🏠"),
    OUTDOOR("Outdoor", "🌳")
}

enum class SceneCategory(
    val displayName: String,
    val emoji: String,
    val environment: EnvironmentType
) {
    // Indoor Categories
    CAFE_INDOOR("Cafe & Coffee", "☕", EnvironmentType.INDOOR),
    COZY_SOFA("Living Room & Sofa", "🛋️", EnvironmentType.INDOOR),
    MIRROR_SELFIE("Mirror & Outfit", "🪞", EnvironmentType.INDOOR),
    BOOKSHELF_WINDOW("Books & Study", "📚", EnvironmentType.INDOOR),
    STUDIO_PORTRAIT("Studio & Wall", "💡", EnvironmentType.INDOOR),

    // Outdoor Categories
    GRADUATION("Graduation & Campus", "🎓", EnvironmentType.OUTDOOR),
    GARDEN_BENCH("Garden & Bench", "🌸", EnvironmentType.OUTDOOR),
    URBAN_STREET("Urban & Street", "🏙️", EnvironmentType.OUTDOOR),
    SCENIC_NATURE("Nature & Vista", "🏔️", EnvironmentType.OUTDOOR),
    BEACH_SUNSET("Beach & Sunset", "🌅", EnvironmentType.OUTDOOR)
}

enum class PoseSilhouetteType {
    // Outdoor: Graduation & Campus
    GRADUATION_KICK_DIPLOMA,
    GRADUATION_CAP_TOSS,
    GRADUATION_PROUD_STAND,
    CAMPUS_STAIRS_SEATED,

    // Outdoor: Garden & Bench
    BENCH_SITTING_CROSSED,
    GARDEN_LEAN_CHIN,
    BENCH_CASUAL_LOOKBACK,
    FLOWER_SMELL_BEND,

    // Outdoor: Urban & Street
    URBAN_STRIDE_WALK,
    URBAN_WALL_LEAN,
    STREET_CURB_SIT,
    CROSSING_LOOKBACK,

    // Outdoor: Nature & Scenic
    SCENIC_ARMS_WIDE,
    CLIFF_EDGE_PERCH,

    // Outdoor: Beach & Sunset
    SUNSET_SILHOUETTE_SIDE,
    BEACH_WALK_BAREFOOT,

    // Indoor: Cafe & Lifestyle
    CAFE_TABLE_PROP,
    CAFE_WINDOW_SIP,
    CAFE_CHEEK_REST,

    // Indoor: Cozy Living Room & Sofa
    SOFA_LEAN_CUSHION,
    SOFA_CHILL_STRETCH,
    RUG_CROSS_LEGGED,

    // Indoor: Mirror & Outfit
    MIRROR_PHONE_TILT,
    MIRROR_CROUCH_LOW,

    // Indoor: Books & Window
    BOOK_PULL_SHELF,
    READING_IN_LIGHT,

    // Indoor: Studio Portrait
    STUDIO_HANDS_POCKET,
    STUDIO_CROSS_ARMS
}

data class PoseAnnotation(
    val text: String,
    val targetNormX: Float, // 0f..1f relative to silhouette bounding box
    val targetNormY: Float, // 0f..1f relative to silhouette bounding box
    val labelOffsetX: Float, // dp offset from target
    val labelOffsetY: Float  // dp offset from target
)

data class PoseRecommendation(
    val id: String,
    val title: String,
    val category: SceneCategory,
    val silhouetteType: PoseSilhouetteType,
    val tips: String,
    val annotations: List<PoseAnnotation>,
    val tags: List<String>
) {
    val environment: EnvironmentType get() = category.environment
}

object PoseCatalog {
    val ALL_POSES: List<PoseRecommendation> = listOf(
        // ==========================================
        // OUTDOOR: Graduation Poses (matches image 1)
        // ==========================================
        PoseRecommendation(
            id = "grad_1",
            title = "Diploma Kick & Spread",
            category = SceneCategory.GRADUATION,
            silhouetteType = PoseSilhouetteType.GRADUATION_KICK_DIPLOMA,
            tips = "Raise diploma high in one hand and kick out the opposite leg with celebration joy!",
            annotations = listOf(
                PoseAnnotation("Raise Diploma", 0.18f, 0.28f, -28f, -24f),
                PoseAnnotation("Spread Hand", 0.78f, 0.18f, 24f, -20f),
                PoseAnnotation("Lift Leg", 0.88f, 0.76f, 22f, -14f)
            ),
            tags = listOf("Joyful", "Celebration", "Full Body")
        ),
        PoseRecommendation(
            id = "grad_2",
            title = "Cap Toss & Cheers",
            category = SceneCategory.GRADUATION,
            silhouetteType = PoseSilhouetteType.GRADUATION_CAP_TOSS,
            tips = "Reach both arms upwards tossing your mortarboard cap into the clear sky.",
            annotations = listOf(
                PoseAnnotation("Toss Cap Up", 0.5f, 0.12f, 0f, -28f),
                PoseAnnotation("Head Tilted Up", 0.5f, 0.28f, 28f, -12f),
                PoseAnnotation("Step Forward", 0.62f, 0.88f, 26f, 8f)
            ),
            tags = listOf("Classic", "Action", "Graduation")
        ),
        PoseRecommendation(
            id = "grad_3",
            title = "Proud Scholar",
            category = SceneCategory.GRADUATION,
            silhouetteType = PoseSilhouetteType.GRADUATION_PROUD_STAND,
            tips = "Stand confident at a 45° angle, holding your graduation diploma across your chest.",
            annotations = listOf(
                PoseAnnotation("Chin Up", 0.5f, 0.2f, 26f, -16f),
                PoseAnnotation("Hold Diploma", 0.52f, 0.44f, 28f, 0f),
                PoseAnnotation("Shoulders Back", 0.32f, 0.36f, -32f, 0f)
            ),
            tags = listOf("Portrait", "Elegant", "Formal")
        ),
        PoseRecommendation(
            id = "grad_4",
            title = "Campus Stairs Sit",
            category = SceneCategory.GRADUATION,
            silhouetteType = PoseSilhouetteType.CAMPUS_STAIRS_SEATED,
            tips = "Sit casually on the campus hall marble steps with one knee pulled up and diploma on lap.",
            annotations = listOf(
                PoseAnnotation("Rest on Step", 0.35f, 0.52f, -28f, -14f),
                PoseAnnotation("Diploma on Lap", 0.5f, 0.65f, 28f, 0f),
                PoseAnnotation("Knee Bent Up", 0.62f, 0.74f, 26f, 0f)
            ),
            tags = listOf("Candid", "Steps", "Outdoor")
        ),

        // ==========================================
        // OUTDOOR: Garden & Bench Poses (matches image 2)
        // ==========================================
        PoseRecommendation(
            id = "bench_1",
            title = "Relaxed Bench Cross",
            category = SceneCategory.GARDEN_BENCH,
            silhouetteType = PoseSilhouetteType.BENCH_SITTING_CROSSED,
            tips = "Drape one arm along the bench backrest, cross your legs comfortably and relax your shoulders.",
            annotations = listOf(
                PoseAnnotation("Arm on Backrest", 0.28f, 0.34f, -32f, -18f),
                PoseAnnotation("Cross Legs", 0.46f, 0.62f, -26f, 0f),
                PoseAnnotation("Angle Foot", 0.54f, 0.9f, -30f, 10f)
            ),
            tags = listOf("Relaxed", "Seated", "Casual")
        ),
        PoseRecommendation(
            id = "garden_2",
            title = "Floral Lean & Smile",
            category = SceneCategory.GARDEN_BENCH,
            silhouetteType = PoseSilhouetteType.GARDEN_LEAN_CHIN,
            tips = "Lean gently towards the flower backdrop with one hand resting softly near your collar or chin.",
            annotations = listOf(
                PoseAnnotation("Hand Near Chin", 0.62f, 0.32f, 26f, -14f),
                PoseAnnotation("Gentle Torso Tilt", 0.44f, 0.46f, -28f, 0f),
                PoseAnnotation("Soft Eye Contact", 0.5f, 0.22f, 28f, -20f)
            ),
            tags = listOf("Soft", "Garden", "Portrait")
        ),
        PoseRecommendation(
            id = "bench_3",
            title = "Over-Shoulder Glance",
            category = SceneCategory.GARDEN_BENCH,
            silhouetteType = PoseSilhouetteType.BENCH_CASUAL_LOOKBACK,
            tips = "Sit angled on the bench and turn your gaze naturally back towards the camera lens.",
            annotations = listOf(
                PoseAnnotation("Turn Head 60°", 0.54f, 0.24f, 28f, -18f),
                PoseAnnotation("Hand on Bench", 0.32f, 0.52f, -28f, 0f)
            ),
            tags = listOf("Candid", "Seated", "Artistic")
        ),
        PoseRecommendation(
            id = "garden_4",
            title = "Flower Blossom Touch",
            category = SceneCategory.GARDEN_BENCH,
            silhouetteType = PoseSilhouetteType.FLOWER_SMELL_BEND,
            tips = "Gently hold a flower stem near your face and inhale with closed eyes or a gentle smile.",
            annotations = listOf(
                PoseAnnotation("Hold Blossom", 0.64f, 0.34f, 28f, -14f),
                PoseAnnotation("Soft Head Tilt", 0.48f, 0.22f, -28f, -18f)
            ),
            tags = listOf("Nature", "Close Up", "Botanical")
        ),

        // ==========================================
        // OUTDOOR: Urban & Street Poses
        // ==========================================
        PoseRecommendation(
            id = "street_1",
            title = "Crosswalk Stride",
            category = SceneCategory.URBAN_STREET,
            silhouetteType = PoseSilhouetteType.URBAN_STRIDE_WALK,
            tips = "Walk naturally forward with confidence, letting arms swing gently as if crossing the street.",
            annotations = listOf(
                PoseAnnotation("Forward Gaze", 0.52f, 0.2f, 26f, -18f),
                PoseAnnotation("Natural Swing", 0.28f, 0.48f, -30f, 0f),
                PoseAnnotation("Stride Step", 0.66f, 0.82f, 26f, 8f)
            ),
            tags = listOf("Dynamic", "Street", "Fashion")
        ),
        PoseRecommendation(
            id = "street_2",
            title = "Textured Wall Lean",
            category = SceneCategory.URBAN_STREET,
            silhouetteType = PoseSilhouetteType.URBAN_WALL_LEAN,
            tips = "Lean back shoulder against a textured wall or pillar, place one hand in your pocket.",
            annotations = listOf(
                PoseAnnotation("Rest Shoulder", 0.35f, 0.34f, -30f, -16f),
                PoseAnnotation("Hand in Pocket", 0.4f, 0.54f, -28f, 0f),
                PoseAnnotation("Knee Bent", 0.58f, 0.74f, 26f, 0f)
            ),
            tags = listOf("Cool", "Editorial", "Minimal")
        ),
        PoseRecommendation(
            id = "street_3",
            title = "City Curb Chill",
            category = SceneCategory.URBAN_STREET,
            silhouetteType = PoseSilhouetteType.STREET_CURB_SIT,
            tips = "Sit down on the street curb with knees bent, resting forearms on knees looking candidly.",
            annotations = listOf(
                PoseAnnotation("Rest on Knees", 0.48f, 0.54f, 26f, -14f),
                PoseAnnotation("Sneaker Forward", 0.66f, 0.86f, 24f, 0f),
                PoseAnnotation("Casual Head Tilt", 0.46f, 0.26f, -28f, -16f)
            ),
            tags = listOf("Streetwear", "Urban", "Casual")
        ),
        PoseRecommendation(
            id = "street_4",
            title = "Over-Shoulder Crossing",
            category = SceneCategory.URBAN_STREET,
            silhouetteType = PoseSilhouetteType.CROSSING_LOOKBACK,
            tips = "Walk away from camera and turn your face over your shoulder with an inviting smile.",
            annotations = listOf(
                PoseAnnotation("Look Back", 0.52f, 0.24f, 28f, -16f),
                PoseAnnotation("Walking Motion", 0.42f, 0.82f, -28f, 0f)
            ),
            tags = listOf("Motion", "Candid", "Lifestyle")
        ),

        // ==========================================
        // OUTDOOR: Nature & Scenic Vistas
        // ==========================================
        PoseRecommendation(
            id = "nature_1",
            title = "Embrace the Horizon",
            category = SceneCategory.SCENIC_NATURE,
            silhouetteType = PoseSilhouetteType.SCENIC_ARMS_WIDE,
            tips = "Open both arms wide embracing the mountain, forest or expansive ocean vista.",
            annotations = listOf(
                PoseAnnotation("Open Left Arm", 0.14f, 0.35f, -28f, -18f),
                PoseAnnotation("Open Right Arm", 0.86f, 0.35f, 28f, -18f),
                PoseAnnotation("Chest Up", 0.5f, 0.38f, 0f, 26f)
            ),
            tags = listOf("Epic", "Travel", "Landscape")
        ),
        PoseRecommendation(
            id = "nature_2",
            title = "Summit Rock Perch",
            category = SceneCategory.SCENIC_NATURE,
            silhouetteType = PoseSilhouetteType.CLIFF_EDGE_PERCH,
            tips = "Sit on a scenic stone or log with one leg draped down and hands propped behind for support.",
            annotations = listOf(
                PoseAnnotation("Support Arms", 0.32f, 0.56f, -28f, 0f),
                PoseAnnotation("Gaze at Horizon", 0.52f, 0.24f, 28f, -16f),
                PoseAnnotation("Leg Draped", 0.58f, 0.84f, 26f, 0f)
            ),
            tags = listOf("Hike", "Adventure", "Landscape")
        ),

        // ==========================================
        // OUTDOOR: Beach & Sunset
        // ==========================================
        PoseRecommendation(
            id = "beach_1",
            title = "Sunset Profile Glow",
            category = SceneCategory.BEACH_SUNSET,
            silhouetteType = PoseSilhouetteType.SUNSET_SILHOUETTE_SIDE,
            tips = "Turn sideways towards the golden sunset, letting the warm rim light trace your profile.",
            annotations = listOf(
                PoseAnnotation("Face Sunset", 0.58f, 0.24f, 28f, -16f),
                PoseAnnotation("Hand in Hair", 0.44f, 0.20f, -28f, -14f),
                PoseAnnotation("Flowing Stance", 0.46f, 0.82f, -26f, 0f)
            ),
            tags = listOf("Golden Hour", "Sunset", "Silhouettes")
        ),
        PoseRecommendation(
            id = "beach_2",
            title = "Barefoot Shoreline Walk",
            category = SceneCategory.BEACH_SUNSET,
            silhouetteType = PoseSilhouetteType.BEACH_WALK_BAREFOOT,
            tips = "Walk along the wet sand by the water foam, holding sandals or bag with carefree motion.",
            annotations = listOf(
                PoseAnnotation("Hold Sandals", 0.32f, 0.52f, -28f, 0f),
                PoseAnnotation("Step into Foam", 0.60f, 0.88f, 26f, 0f),
                PoseAnnotation("Carefree Smile", 0.50f, 0.22f, 26f, -16f)
            ),
            tags = listOf("Beach", "Vacation", "Summer")
        ),

        // ==========================================
        // INDOOR: Cafe & Lifestyle
        // ==========================================
        PoseRecommendation(
            id = "cafe_1",
            title = "Table Lean & Warm Mug",
            category = SceneCategory.CAFE_INDOOR,
            silhouetteType = PoseSilhouetteType.CAFE_TABLE_PROP,
            tips = "Rest your forearms comfortably on the cafe table, holding your warm mug or looking down thoughtfully.",
            annotations = listOf(
                PoseAnnotation("Rest on Elbows", 0.35f, 0.52f, -28f, -12f),
                PoseAnnotation("Hold Coffee Mug", 0.5f, 0.58f, 28f, 0f),
                PoseAnnotation("Warm Smile", 0.5f, 0.28f, 28f, -20f)
            ),
            tags = listOf("Cozy", "Indoor", "Lifestyle")
        ),
        PoseRecommendation(
            id = "cafe_2",
            title = "Window Sip & Contemplate",
            category = SceneCategory.CAFE_INDOOR,
            silhouetteType = PoseSilhouetteType.CAFE_WINDOW_SIP,
            tips = "Sit next to the coffee shop window, raising your cup close to lips while gazing outside.",
            annotations = listOf(
                PoseAnnotation("Lift Cup to Lips", 0.54f, 0.32f, 28f, -12f),
                PoseAnnotation("Gaze Out Glass", 0.62f, 0.22f, 28f, -18f),
                PoseAnnotation("Arm on Table", 0.36f, 0.56f, -28f, 0f)
            ),
            tags = listOf("Atmospheric", "Cafe", "Cinematic")
        ),
        PoseRecommendation(
            id = "cafe_3",
            title = "Cheek-in-Palm Candid",
            category = SceneCategory.CAFE_INDOOR,
            silhouetteType = PoseSilhouetteType.CAFE_CHEEK_REST,
            tips = "Rest one cheek comfortably in your open palm with elbow propped on table, looking right at lens.",
            annotations = listOf(
                PoseAnnotation("Cheek in Palm", 0.56f, 0.30f, 28f, -14f),
                PoseAnnotation("Direct Eye Contact", 0.48f, 0.24f, -28f, -18f),
                PoseAnnotation("Relaxed Shoulder", 0.34f, 0.42f, -28f, 0f)
            ),
            tags = listOf("Intimate", "Portrait", "Cafe")
        ),

        // ==========================================
        // INDOOR: Cozy Living Room & Couch
        // ==========================================
        PoseRecommendation(
            id = "sofa_1",
            title = "Cushion Hug & Side Tuck",
            category = SceneCategory.COZY_SOFA,
            silhouetteType = PoseSilhouetteType.SOFA_LEAN_CUSHION,
            tips = "Curl up sideways on the sofa hugging a soft pillow or cushion with knees tucked comfortably.",
            annotations = listOf(
                PoseAnnotation("Hug Cushion", 0.48f, 0.48f, 26f, -12f),
                PoseAnnotation("Tuck Knees", 0.64f, 0.66f, 26f, 0f),
                PoseAnnotation("Cozy Lean", 0.36f, 0.32f, -28f, -14f)
            ),
            tags = listOf("Cozy", "Home", "Relaxed")
        ),
        PoseRecommendation(
            id = "sofa_2",
            title = "Casual Recline Stretch",
            category = SceneCategory.COZY_SOFA,
            silhouetteType = PoseSilhouetteType.SOFA_CHILL_STRETCH,
            tips = "Recline back on the sofa armrest with legs stretched across cushions and one arm draped over head.",
            annotations = listOf(
                PoseAnnotation("Arm Behind Head", 0.38f, 0.28f, -28f, -16f),
                PoseAnnotation("Legs Stretched", 0.72f, 0.68f, 26f, 0f)
            ),
            tags = listOf("Weekend", "Chilled", "Comfort")
        ),
        PoseRecommendation(
            id = "sofa_3",
            title = "Floor Rug Cross-Legged",
            category = SceneCategory.COZY_SOFA,
            silhouetteType = PoseSilhouetteType.RUG_CROSS_LEGGED,
            tips = "Sit down on the carpet or rug leaning against the coffee table or sofa with crossed legs.",
            annotations = listOf(
                PoseAnnotation("Crossed Legs", 0.5f, 0.78f, 0f, 24f),
                PoseAnnotation("Lean Forward", 0.5f, 0.38f, 26f, -12f),
                PoseAnnotation("Hands on Knees", 0.32f, 0.68f, -28f, 0f)
            ),
            tags = listOf("Floor", "Playful", "Home")
        ),

        // ==========================================
        // INDOOR: Mirror Selfie & Outfit
        // ==========================================
        PoseRecommendation(
            id = "mirror_1",
            title = "Outfit Check & Hip Pop",
            category = SceneCategory.MIRROR_SELFIE,
            silhouetteType = PoseSilhouetteType.MIRROR_PHONE_TILT,
            tips = "Hold your phone at chest level, pop one hip to the side, and keep the opposite foot forward.",
            annotations = listOf(
                PoseAnnotation("Phone at Chest", 0.52f, 0.38f, 28f, -12f),
                PoseAnnotation("Pop Hip Out", 0.40f, 0.56f, -28f, 0f),
                PoseAnnotation("Foot Pointed", 0.62f, 0.88f, 26f, 0f)
            ),
            tags = listOf("OOTD", "Mirror", "Fashion")
        ),
        PoseRecommendation(
            id = "mirror_2",
            title = "Aesthetic Crouch Squat",
            category = SceneCategory.MIRROR_SELFIE,
            silhouetteType = PoseSilhouetteType.MIRROR_CROUCH_LOW,
            tips = "Squat low on one knee before the full-length mirror, tilting phone slightly downward for dynamic angle.",
            annotations = listOf(
                PoseAnnotation("Low Squat Knee", 0.36f, 0.72f, -28f, 0f),
                PoseAnnotation("Phone Tilted", 0.56f, 0.44f, 28f, -12f),
                PoseAnnotation("Sneaker Focus", 0.66f, 0.84f, 24f, 0f)
            ),
            tags = listOf("Trendy", "Streetwear", "Low Angle")
        ),

        // ==========================================
        // INDOOR: Bookshelf & Study
        // ==========================================
        PoseRecommendation(
            id = "book_1",
            title = "Bookshelf Reach & Browse",
            category = SceneCategory.BOOKSHELF_WINDOW,
            silhouetteType = PoseSilhouetteType.BOOK_PULL_SHELF,
            tips = "Stand beside the bookshelf reaching one hand to slide out a spine, turning halfway to camera.",
            annotations = listOf(
                PoseAnnotation("Reach for Spine", 0.30f, 0.28f, -28f, -16f),
                PoseAnnotation("Halfway Turn", 0.54f, 0.36f, 28f, 0f),
                PoseAnnotation("Weight on Back Foot", 0.56f, 0.82f, 26f, 0f)
            ),
            tags = listOf("Library", "Intellectual", "Books")
        ),
        PoseRecommendation(
            id = "book_2",
            title = "Sunlit Window Reading",
            category = SceneCategory.BOOKSHELF_WINDOW,
            silhouetteType = PoseSilhouetteType.READING_IN_LIGHT,
            tips = "Sit in the sunny window nook holding an open paperback in both hands, absorbing the sunlight.",
            annotations = listOf(
                PoseAnnotation("Hold Open Book", 0.5f, 0.52f, 28f, 0f),
                PoseAnnotation("Eyes on Page", 0.48f, 0.32f, -28f, -14f),
                PoseAnnotation("Window Backlight", 0.74f, 0.36f, 26f, -16f)
            ),
            tags = listOf("Reading", "Sunny", "Peaceful")
        ),

        // ==========================================
        // INDOOR: Studio & Minimalist Wall
        // ==========================================
        PoseRecommendation(
            id = "studio_1",
            title = "Pocket Thumbs & Stance",
            category = SceneCategory.STUDIO_PORTRAIT,
            silhouetteType = PoseSilhouetteType.STUDIO_HANDS_POCKET,
            tips = "Hook both thumbs into front pockets, relax one shoulder down, and stand tall with confidence.",
            annotations = listOf(
                PoseAnnotation("Thumbs in Pockets", 0.40f, 0.56f, -28f, 0f),
                PoseAnnotation("Dropped Shoulder", 0.62f, 0.34f, 28f, -12f),
                PoseAnnotation("Confident Gaze", 0.50f, 0.20f, 26f, -16f)
            ),
            tags = listOf("Clean", "Studio", "Fashion")
        ),
        PoseRecommendation(
            id = "studio_2",
            title = "Executive Arms Folded",
            category = SceneCategory.STUDIO_PORTRAIT,
            silhouetteType = PoseSilhouetteType.STUDIO_CROSS_ARMS,
            tips = "Fold arms comfortably across chest with slight 30° body turn, chin held level.",
            annotations = listOf(
                PoseAnnotation("Crossed Forearms", 0.50f, 0.46f, 28f, 0f),
                PoseAnnotation("30° Torso Angle", 0.38f, 0.38f, -28f, 0f),
                PoseAnnotation("Level Chin", 0.50f, 0.22f, 26f, -18f)
            ),
            tags = listOf("Professional", "Headshot", "Editorial")
        )
    )

    val INDOOR_POSES: List<PoseRecommendation> by lazy {
        ALL_POSES.filter { it.environment == EnvironmentType.INDOOR }
    }

    val OUTDOOR_POSES: List<PoseRecommendation> by lazy {
        ALL_POSES.filter { it.environment == EnvironmentType.OUTDOOR }
    }
}
