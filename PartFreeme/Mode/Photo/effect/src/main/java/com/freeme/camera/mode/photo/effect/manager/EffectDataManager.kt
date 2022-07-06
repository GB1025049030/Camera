package com.freeme.camera.mode.photo.effect.manager

import com.freeme.camera.mode.photo.effect.R
import com.freeme.camera.mode.photo.effect.model.EffectType
import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem
import com.freeme.camera.mode.photo.effect.mvi.bean.ColorItem
import com.freeme.camera.mode.photo.effect.mvi.bean.ComposerNode
import java.util.*

/**
 * {zh}
 * 特效数据管理类，负责管理美颜美妆等功能的数据
 */
/** {en}
 * Special effects data management class, responsible for managing the data of beauty and makeup functions
 */
class EffectDataManager(private val mEffectType: EffectType) {
    private val mSavedItems: MutableMap<Int, ButtonItem> = HashMap()
    fun generateComposerNodesAndTags(selectNodes: Set<ButtonItem>): Array<Array<String?>> {
        val items: MutableList<ButtonItem> = ArrayList()
        val set: MutableSet<String> = HashSet()
        for (item in selectNodes) {
            if (item.node != null && !set.contains(item.node!!.path)) {
                set.add(item.node!!.path)
                items.add(item)
            }
        }
        val nodes = arrayOfNulls<String>(items.size)
        val tags = arrayOfNulls<String>(items.size)
        for (i in items.indices) {
            nodes[i] = items[i].node!!.path
            tags[i] = items[i].node!!.tag
        }
        return arrayOf(nodes, tags)
    }

    fun generateComposerNodesAndTags(item: ButtonItem?): Array<Array<String?>> {
        val nodes = arrayOfNulls<String>(1)
        val tags = arrayOfNulls<String>(1)
        nodes[0] = if (item == null || item.node == null) "" else item.node!!.path
        tags[0] = if (item == null || item.node == null) "" else item.node!!.tag
        return arrayOf(nodes, tags)
    }

    val defaultItems: Set<ButtonItem>
        get() {
            val beautyFace = getItem(TYPE_BEAUTY_FACE)
            val beautyReshape = getItem(TYPE_BEAUTY_RESHAPE)
            resetAll()
            val items: MutableSet<ButtonItem> = HashSet()
            for (item in beautyFace!!.children) {
                if (isDefaultEffect(item.id)) {
                    item.isSelected = true
                    items.add(item)
                }
            }
            for (item in beautyReshape!!.children) {
                if (item.hasChildren()) {
                    for (child in item.children) {
                        if (isDefaultEffect(child.id)) {
                            child.isSelected = true
                            items.add(child)
                        }
                    }
                }
            }
            return items
        }

    fun resetAll() {
        for (item in allItems()) {
            resetItem(item)
        }
    }

    fun resetItem(item: ButtonItem) {
        if (item.hasChildren()) {
            for (child in item.children) {
                resetItem(child)
            }
        }
        item.selectChild = null
        item.isSelected = false
        item.intensityArray = getDefaultIntensity(item.id, mEffectType)
    }

    companion object {
        const val OFFSET = 16
        const val MASK = 0xFFFF.inv()
        const val SUB_OFFSET = 8
        const val SUB_MASK = 0xFF.inv()

        //   {zh} 一级菜单       {en} Level 1 menu
        //The second menu
        const val TYPE_CLOSE = -1

        //   {zh} Beautify face 美颜       {en} Beautify face
        const val TYPE_BEAUTY_FACE = 1 shl OFFSET

        //   {zh} Beautify reshape 美型       {en} Beautified reshape
        const val TYPE_BEAUTY_RESHAPE = 2 shl OFFSET

        //   {zh} Beautify body 美体       {en} Beautify the body
        const val TYPE_BEAUTY_BODY = 3 shl OFFSET

        //   {zh} Makeup 美妆       {en} Makeup beauty
        const val TYPE_MAKEUP = 4 shl OFFSET

        //   {zh} Filter 滤镜       {en} Filter
        const val TYPE_FILTER = 5 shl OFFSET

        //   {zh} 画质      {en} Palette
        const val TYPE_PALETTE = 6 shl OFFSET

        //   {zh} 口红      {en} Lipstick
        const val TYPE_LIPSTICK = 7 shl OFFSET

        //   {zh} 染发      {en} Hair dye
        const val TYPE_HAIR_DYE = 8 shl OFFSET

        //   {zh} 风格妆       {en} Style makeup
        const val TYPE_STYLE_MAKEUP = 9 shl OFFSET

        //   {zh} 二级菜单       {en} Secondary menu
        //The secondary menu
        //   {zh} Beautify face 美颜       {en} Beautify face
        const val TYPE_BEAUTY_FACE_SMOOTH = TYPE_BEAUTY_FACE + (1 shl SUB_OFFSET)
        const val TYPE_BEAUTY_FACE_WHITEN = TYPE_BEAUTY_FACE + (2 shl SUB_OFFSET)
        const val TYPE_BEAUTY_FACE_SHARPEN = TYPE_BEAUTY_FACE + (3 shl SUB_OFFSET)

        //   {zh} Beautify reshape 美形       {en} Beautify reshape
        //  {zh} 面部  {en} Facial
        const val TYPE_BEAUTY_RESHAPE_FACE =
            TYPE_BEAUTY_RESHAPE + (1 shl SUB_OFFSET) // {zh} 面部 {en} Facial
        const val TYPE_BEAUTY_RESHAPE_FACE_OVERALL =
            TYPE_BEAUTY_RESHAPE + (2 shl SUB_OFFSET) // {zh} 瘦脸 {en} Skinny face
        const val TYPE_BEAUTY_RESHAPE_FACE_SMALL =
            TYPE_BEAUTY_RESHAPE + (3 shl SUB_OFFSET) // {zh} 小脸 {en} Little face
        const val TYPE_BEAUTY_RESHAPE_FACE_CUT =
            TYPE_BEAUTY_RESHAPE + (4 shl SUB_OFFSET) // {zh} 窄脸 {en} Narrow face
        const val TYPE_BEAUTY_RESHAPE_FACE_V =
            TYPE_BEAUTY_RESHAPE + (5 shl SUB_OFFSET) // {zh} V脸 {en} V face
        const val TYPE_BEAUTY_RESHAPE_FOREHEAD =
            TYPE_BEAUTY_RESHAPE + (6 shl SUB_OFFSET) // {zh} 额头/发际线 {en} Forehead/hairline
        const val TYPE_BEAUTY_RESHAPE_CHEEK =
            TYPE_BEAUTY_RESHAPE + (7 shl SUB_OFFSET) // {zh} 颧骨 {en} Cheekbones
        const val TYPE_BEAUTY_RESHAPE_JAW =
            TYPE_BEAUTY_RESHAPE + (8 shl SUB_OFFSET) // {zh} 下颌骨 {en} Mandible
        const val TYPE_BEAUTY_RESHAPE_CHIN =
            TYPE_BEAUTY_RESHAPE + (9 shl SUB_OFFSET) // {zh} 下巴 {en} Chin

        //  {zh} 眼睛  {en} Eyes
        const val TYPE_BEAUTY_RESHAPE_EYE =
            TYPE_BEAUTY_RESHAPE + (20 shl SUB_OFFSET) // {zh} 眼睛 {en} Eyes
        const val TYPE_BEAUTY_RESHAPE_EYE_SIZE =
            TYPE_BEAUTY_RESHAPE + (21 shl SUB_OFFSET) // {zh} 大眼 {en} Big eyes
        const val TYPE_BEAUTY_RESHAPE_EYE_HEIGHT =
            TYPE_BEAUTY_RESHAPE + (22 shl SUB_OFFSET) // {zh} 眼高度 {en} Eye height
        const val TYPE_BEAUTY_RESHAPE_EYE_WIDTH =
            TYPE_BEAUTY_RESHAPE + (23 shl SUB_OFFSET) // {zh} 眼宽度 {en} Eye width
        const val TYPE_BEAUTY_RESHAPE_EYE_MOVE =
            TYPE_BEAUTY_RESHAPE + (24 shl SUB_OFFSET) // {zh} 眼移动/眼位置 {en} Eye movement/eye position
        const val TYPE_BEAUTY_RESHAPE_EYE_SPACING =
            TYPE_BEAUTY_RESHAPE + (25 shl SUB_OFFSET) // {zh} 眼间距 {en} Eye spacing
        const val TYPE_BEAUTY_RESHAPE_EYE_LOWER_EYELID =
            TYPE_BEAUTY_RESHAPE + (26 shl SUB_OFFSET) // {zh} 眼睑下至 {en} Eyelid down to
        const val TYPE_BEAUTY_RESHAPE_EYE_PUPIL =
            TYPE_BEAUTY_RESHAPE + (27 shl SUB_OFFSET) // {zh} 瞳孔大小 {en} Pupil size
        const val TYPE_BEAUTY_RESHAPE_EYE_INNER_CORNER =
            TYPE_BEAUTY_RESHAPE + (28 shl SUB_OFFSET) // {zh} 内眼角 {en} Inner canthus
        const val TYPE_BEAUTY_RESHAPE_EYE_OUTER_CORNER =
            TYPE_BEAUTY_RESHAPE + (28 shl SUB_OFFSET) // {zh} 外眼角 {en} Outer corner of eye
        const val TYPE_BEAUTY_RESHAPE_EYE_ROTATE =
            TYPE_BEAUTY_RESHAPE + (29 shl SUB_OFFSET) // {zh} 眼角度、眼角上扬 {en} Eye angle, canthus rise

        // {zh} 鼻子 {en} Nose
        const val TYPE_BEAUTY_RESHAPE_NOSE =
            TYPE_BEAUTY_RESHAPE + (40 shl SUB_OFFSET) // {zh} 鼻子 {en} Nose
        const val TYPE_BEAUTY_RESHAPE_NOSE_SIZE =
            TYPE_BEAUTY_RESHAPE + (41 shl SUB_OFFSET) // {zh} 鼻子大小/瘦鼻 {en} Nose size/thin nose
        const val TYPE_BEAUTY_RESHAPE_NOSE_SWING =
            TYPE_BEAUTY_RESHAPE + (42 shl SUB_OFFSET) // {zh} 鼻翼 {en} Nose
        const val TYPE_BEAUTY_RESHAPE_NOSE_BRIDGE =
            TYPE_BEAUTY_RESHAPE + (43 shl SUB_OFFSET) // {zh} 鼻梁 {en} Bridge of nose
        const val TYPE_BEAUTY_RESHAPE_NOSE_MOVE =
            TYPE_BEAUTY_RESHAPE + (44 shl SUB_OFFSET) // {zh} 鼻子提升/长鼻 {en} Nose lift/long nose
        const val TYPE_BEAUTY_RESHAPE_NOSE_TIP =
            TYPE_BEAUTY_RESHAPE + (45 shl SUB_OFFSET) // {zh} 鼻尖 {en} Nose tip
        const val TYPE_BEAUTY_RESHAPE_NOSE_ROOT =
            TYPE_BEAUTY_RESHAPE + (46 shl SUB_OFFSET) // {zh} 山根 {en} Yamagata

        //  {zh} 眉毛  {en} Eyebrows
        const val TYPE_BEAUTY_RESHAPE_BROW =
            TYPE_BEAUTY_RESHAPE + (60 shl SUB_OFFSET) //  {zh} 眉毛  {en} Eyebrows
        const val TYPE_BEAUTY_RESHAPE_BROW_SIZE =
            TYPE_BEAUTY_RESHAPE + (61 shl SUB_OFFSET) //  {zh} 眉毛粗细  {en} Eyebrow thickness
        const val TTYPE_BEAUTY_RESHAPE_BROW_POSITION =
            TYPE_BEAUTY_RESHAPE + (62 shl SUB_OFFSET) //  {zh} 眉毛位置  {en} Eyebrow position
        const val TYPE_BEAUTY_RESHAPE_BROW_TILT =
            TYPE_BEAUTY_RESHAPE + (63 shl SUB_OFFSET) //  {zh} 眉毛倾斜  {en} Tilted eyebrows
        const val TYPE_BEAUTY_RESHAPE_BROW_RIDGE =
            TYPE_BEAUTY_RESHAPE + (64 shl SUB_OFFSET) //  {zh} 眉峰  {en} Meifeng
        const val TYPE_BEAUTY_RESHAPE_BROW_DISTANCE =
            TYPE_BEAUTY_RESHAPE + (65 shl SUB_OFFSET) //  {zh} 眉毛间距  {en} Eyebrow spacing
        const val TYPE_BEAUTY_RESHAPE_BROW_WIDTH =
            TYPE_BEAUTY_RESHAPE + (66 shl SUB_OFFSET) //  {zh} 眉毛宽度  {en} Eyebrow width

        // {zh} 嘴巴 {en} Mouth
        const val TYPE_BEAUTY_RESHAPE_MOUTH =
            TYPE_BEAUTY_RESHAPE + (80 shl SUB_OFFSET) //  {zh} 嘴巴  {en} Mouth
        const val TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM =
            TYPE_BEAUTY_RESHAPE + (81 shl SUB_OFFSET) //  {zh} 嘴巴大小/嘴形  {en} Mouth size/shape
        const val TYPE_BEAUTY_RESHAPE_MOUTH_WIDTH =
            TYPE_BEAUTY_RESHAPE + (82 shl SUB_OFFSET) //  {zh} 嘴巴宽度  {en} Mouth width
        const val TYPE_BEAUTY_RESHAPE_MOUTH_MOVE =
            TYPE_BEAUTY_RESHAPE + (83 shl SUB_OFFSET) //  {zh} 嘴巴位置/人中  {en} Mouth position/person
        const val TYPE_BEAUTY_RESHAPE_MOUTH_SMILE =
            TYPE_BEAUTY_RESHAPE + (84 shl SUB_OFFSET) //  {zh} 微笑  {en} Smile

        // {zh} 妆 {en} Makeup
        const val TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE = TYPE_BEAUTY_RESHAPE + (35 shl SUB_OFFSET)
        const val TYPE_BEAUTY_RESHAPE_REMOVE_POUCH = TYPE_BEAUTY_RESHAPE + (36 shl SUB_OFFSET)
        const val TYPE_BEAUTY_RESHAPE_SMILE_FOLDS = TYPE_BEAUTY_RESHAPE + (37 shl SUB_OFFSET)
        const val TYPE_BEAUTY_RESHAPE_WHITEN_TEETH = TYPE_BEAUTY_RESHAPE + (38 shl SUB_OFFSET)
        const val TYPE_BEAUTY_RESHAPE_SINGLE_TO_DOUBLE_EYELID =
            TYPE_BEAUTY_RESHAPE + (39 shl SUB_OFFSET)

        //
        //   {zh} Beautify body 美体       {en} Beautify the body
        const val TYPE_BEAUTY_BODY_THIN = TYPE_BEAUTY_BODY + (1 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_LONG_LEG = TYPE_BEAUTY_BODY + (2 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_SLIM_LEG = TYPE_BEAUTY_BODY + (3 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_SLIM_WAIST = TYPE_BEAUTY_BODY + (4 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_ENLARGE_BREAST = TYPE_BEAUTY_BODY + (5 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_ENHANCE_HIP = TYPE_BEAUTY_BODY + (6 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_ENHANCE_NECK = TYPE_BEAUTY_BODY + (7 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_SLIM_ARM = TYPE_BEAUTY_BODY + (8 shl SUB_OFFSET)
        const val TYPE_BEAUTY_BODY_SHRINK_HEAD = TYPE_BEAUTY_BODY + (9 shl SUB_OFFSET)

        //   {zh} Makeup 美妆       {en} Makeup beauty
        const val TYPE_MAKEUP_LIP = TYPE_MAKEUP + (1 shl SUB_OFFSET)
        const val TYPE_MAKEUP_BLUSHER = TYPE_MAKEUP + (2 shl SUB_OFFSET)
        const val TYPE_MAKEUP_EYELASH = TYPE_MAKEUP + (3 shl SUB_OFFSET)
        const val TYPE_MAKEUP_PUPIL = TYPE_MAKEUP + (4 shl SUB_OFFSET)
        const val TYPE_MAKEUP_HAIR = TYPE_MAKEUP + (5 shl SUB_OFFSET)
        const val TYPE_MAKEUP_EYESHADOW = TYPE_MAKEUP + (6 shl SUB_OFFSET)
        const val TYPE_MAKEUP_EYEBROW = TYPE_MAKEUP + (7 shl SUB_OFFSET)
        const val TYPE_MAKEUP_FACIAL = TYPE_MAKEUP + (8 shl SUB_OFFSET)
        const val TYPE_MAKEUP_WOCAN = TYPE_MAKEUP + (9 shl SUB_OFFSET)
        const val TYPE_MAKEUP_EYELIGHT = TYPE_MAKEUP + (10 shl SUB_OFFSET)

        //   {zh} 风格妆       {en} Style makeup
        const val TYPE_STYLE_MAKEUP_2D = TYPE_STYLE_MAKEUP + (1 shl SUB_OFFSET)
        const val TYPE_STYLE_MAKEUP_3D = TYPE_STYLE_MAKEUP + (2 shl SUB_OFFSET)

        //   {zh} 口红       {en} Lipstick
        const val TYPE_LIPSTICK_GLOSSY = TYPE_LIPSTICK + (1 shl SUB_OFFSET)
        const val TYPE_LIPSTICK_MATTE = TYPE_LIPSTICK + (2 shl SUB_OFFSET)

        //   {zh} 染发       {en} Hair dye
        const val TYPE_HAIR_DYE_FULL = TYPE_HAIR_DYE + (1 shl SUB_OFFSET)
        const val TYPE_HAIR_DYE_HIGHLIGHT = TYPE_HAIR_DYE + (2 shl SUB_OFFSET)

        //   {zh} 画质       {en} Palette
        const val TYPE_PALETTE_TEMPERATURE = TYPE_PALETTE + (1 shl SUB_OFFSET)
        const val TYPE_PALETTE_TONE = TYPE_PALETTE + (2 shl SUB_OFFSET)
        const val TYPE_PALETTE_SATURATION = TYPE_PALETTE + (3 shl SUB_OFFSET)
        const val TYPE_PALETTE_BRIGHTNESS = TYPE_PALETTE + (4 shl SUB_OFFSET)
        const val TYPE_PALETTE_CONTRAST = TYPE_PALETTE + (5 shl SUB_OFFSET)
        const val TYPE_PALETTE_HIGHLIGHT = TYPE_PALETTE + (6 shl SUB_OFFSET)
        const val TYPE_PALETTE_SHADOW = TYPE_PALETTE + (7 shl SUB_OFFSET)
        const val TYPE_PALETTE_LIGHT_SENSATION = TYPE_PALETTE + (8 shl SUB_OFFSET)
        const val TYPE_PALETTE_PARTICLE = TYPE_PALETTE + (9 shl SUB_OFFSET)
        const val TYPE_PALETTE_FADE = TYPE_PALETTE + (10 shl SUB_OFFSET)
        const val TYPE_PALETTE_VIGNETTING = TYPE_PALETTE + (11 shl SUB_OFFSET)

        //   {zh} Node name 结点名称       {en} Node name node name
        var NODE_BEAUTY_STANDARD = "beauty_Android_standard"

        //    static String NODE_BEAUTY_LITE = "beauty_Android_lite";
        var NODE_BEAUTY_LITE = "mask_beauty"
        var NODE_BEAUTY_4ITEMS = "beauty_4Items"
        var NODE_RESHAPE_STANDARD = "reshape_standard"
        var NODE_RESHAPE_LITE = "reshape_lite"
        var NODE_ALL_SLIM = "body/allslim"

        //   {zh} 染发部位desc字段       {en} Hair dye parts desc
        const val DESC_HAIR_DYE_HIGHLIGHT_PART_A = 1
        const val DESC_HAIR_DYE_HIGHLIGHT_PART_B = 2
        const val DESC_HAIR_DYE_HIGHLIGHT_PART_C = 3
        const val DESC_HAIR_DYE_HIGHLIGHT_PART_D = 4
        const val DESC_HAIR_DYE_FULL = 5
        const val DESC_HAIR_DYE_HIGHLIGHT = 6
        fun getDefaultIntensity(type: Int, effectType: EffectType?): FloatArray {
            val intensity = getDefaultMap(effectType)!![type]
            if (intensity is Float) {
                return floatArrayOf((intensity as Float?)!!)
            } else if (intensity is FloatArray) {
                return Arrays.copyOf(intensity as FloatArray?, intensity.size)
            }
            return floatArrayOf(0.0f)
        }

        fun getDefaultIntensity(
            type: Int,
            effectType: EffectType?,
            enableNegative: Boolean
        ): FloatArray {
            var intensity = getDefaultIntensity(type, effectType)
            if (enableNegative && intensity[0] == 0f) {
                intensity = floatArrayOf(0.5f)
            }
            return intensity
        }

        private fun getDefaultMap(type: EffectType?): Map<Int, Any>? {
            var type = type
            if (type == null) {
                type = EffectType.LITE_ASIA
            }
            return when (type) {
                EffectType.LITE_ASIA, EffectType.LITE_NOT_ASIA -> DEFAULT_LITE_VALUE
                EffectType.STANDARD_ASIA, EffectType.STANDARD_NOT_ASIA -> DEFAULT_STANDARD_VALUE
            }
            return DEFAULT_STANDARD_VALUE
        }

        private var DEFAULT_STANDARD_VALUE: MutableMap<Int, Any>? = null
        private var DEFAULT_LITE_VALUE: Map<Int, Any>? = null
        private val colorForChooseMap = HashMap<Int, ArrayList<ColorItem>>()
        private fun getColorForChoose(effectType: EffectType, type: Int): List<ColorItem> {
            return if (effectType == EffectType.LITE_ASIA || effectType == EffectType.LITE_NOT_ASIA) emptyList() else colorForChooseMap[type]!!
        }

        private val DefaultLiteEffects: HashSet<Int?> = HashSet()
        private val DefaultStandardEffects: HashSet<Int?> = HashSet()

        init {
            val standardMap: MutableMap<Int, Any?> = HashMap()
            val liteMap: MutableMap<Int, Any> = HashMap()
            //   {zh} 美颜       {en} Beauty
            // beauty face
            standardMap[TYPE_BEAUTY_FACE_SMOOTH] = 0.65f
            standardMap[TYPE_BEAUTY_FACE_WHITEN] = 0.35f
            standardMap[TYPE_BEAUTY_FACE_SHARPEN] = 0.25f
            //   {zh} 美型       {en} Beauty
            // beaury reshape
            //  {zh} 面部  {en} Facial
            standardMap[TYPE_BEAUTY_RESHAPE_FACE_OVERALL] = 0.75f
            standardMap[TYPE_BEAUTY_RESHAPE_CHEEK] = 0.65f
            standardMap[TYPE_BEAUTY_RESHAPE_FOREHEAD] = 0.70f
            standardMap[TYPE_BEAUTY_RESHAPE_SMILE_FOLDS] = 0.5f

            //  {zh} 眼睛  {en} Eyes
            standardMap[TYPE_BEAUTY_RESHAPE_EYE_SIZE] = 0.65f
            standardMap[TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE] = 0.4f
            standardMap[TYPE_BEAUTY_RESHAPE_REMOVE_POUCH] = 0.5f


            //  {zh} 鼻子  {en} Nose
            standardMap[TYPE_BEAUTY_RESHAPE_NOSE_SIZE] = 0.65f
            standardMap[TYPE_BEAUTY_RESHAPE_NOSE_MOVE] = 0.6f


            //  {zh} 眉毛  {en} Eyebrows

            // {zh} 嘴巴 {en} Mouth
            standardMap[TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM] = 0.6f
            standardMap[TYPE_BEAUTY_RESHAPE_MOUTH_MOVE] = 0.65f
            standardMap[TYPE_BEAUTY_RESHAPE_WHITEN_TEETH] = 0.3f


            //   {zh} 美体       {en} Body
            standardMap[TYPE_BEAUTY_BODY_ENHANCE_HIP] = 0.5f

            //   {zh} 美妆       {en} Beauty makeup
            standardMap[TYPE_MAKEUP_LIP] = floatArrayOf(0.5f, 0f, 0f, 0f)
            standardMap[TYPE_MAKEUP_HAIR] = floatArrayOf(0.5f, 0f, 0f, 0f)
            standardMap[TYPE_MAKEUP_BLUSHER] = floatArrayOf(0.5f, 0f, 0f, 0f)
            standardMap[TYPE_MAKEUP_FACIAL] = 0.5f
            standardMap[TYPE_MAKEUP_EYEBROW] = floatArrayOf(0.3f, 0f, 0f, 0f)
            standardMap[TYPE_MAKEUP_EYESHADOW] = 0.5f
            standardMap[TYPE_MAKEUP_PUPIL] = 0.5f
            standardMap[TYPE_MAKEUP_EYELASH] = floatArrayOf(0.5f, 0f, 0f, 0f)
            standardMap[TYPE_MAKEUP_EYELIGHT] = 0.5f
            standardMap[TYPE_MAKEUP_WOCAN] = 0.5f


            //   {zh} 风格妆       {en} Style makeup
            standardMap[TYPE_STYLE_MAKEUP] = floatArrayOf(0.8f, 0.8f)
            standardMap[TYPE_STYLE_MAKEUP_2D] = floatArrayOf(0.8f, 0.8f)
            standardMap[TYPE_STYLE_MAKEUP_3D] = floatArrayOf(0.8f, 0.8f)

            //   {zh} 滤镜       {en} Filter
            // filter
            standardMap[TYPE_FILTER] = 0.8f

//        //  {zh} 画质       {en} Palette
//        standardMap.put(TYPE_PALETTE_TEMPERATURE    , 0.0F);
//        standardMap.put(TYPE_PALETTE_TONE           , 0.0F);
//        standardMap.put(TYPE_PALETTE_SATURATION     , 0.0F);
//        standardMap.put(TYPE_PALETTE_BRIGHTNESS     , 0.0F);
//        standardMap.put(TYPE_PALETTE_CONTRAST       , 0.0F);
//        standardMap.put(TYPE_PALETTE_HIGHLIGHT      , 0.0F);
//        standardMap.put(TYPE_PALETTE_SHADOW         , 0.0F);
//        standardMap.put(TYPE_PALETTE_LIGHT_SENSATION, 0.0F);
//        standardMap.put(TYPE_PALETTE_SHARPEN        , 0.0F);
//        standardMap.put(TYPE_PALETTE_PARTICLE       , 0.0F);
//        standardMap.put(TYPE_PALETTE_FADE           , 0.0F);
//        standardMap.put(TYPE_PALETTE_VIGNETTING     , 0.0F);
            DEFAULT_STANDARD_VALUE = Collections.unmodifiableMap(standardMap)

            //   {zh} 美颜       {en} Beauty
            // beauty face
            liteMap[TYPE_BEAUTY_FACE_SMOOTH] = 0.5f
            liteMap[TYPE_BEAUTY_FACE_WHITEN] = 0.35f
            liteMap[TYPE_BEAUTY_FACE_SHARPEN] = 0.3f

            //   {zh} 美型       {en} Beauty
            // beauty reshape
            liteMap[TYPE_BEAUTY_RESHAPE_FACE_OVERALL] = 0.35f
            liteMap[TYPE_BEAUTY_RESHAPE_CHEEK] = 0.2f
            liteMap[TYPE_BEAUTY_RESHAPE_JAW] = 0.4f
            liteMap[TYPE_BEAUTY_RESHAPE_SMILE_FOLDS] = 0.35f

            //  {zh} 眼睛  {en} Eyes
            liteMap[TYPE_BEAUTY_RESHAPE_EYE_SIZE] = 0.35f
            liteMap[TYPE_BEAUTY_RESHAPE_EYE_SPACING] = 0.15f
            liteMap[TYPE_BEAUTY_RESHAPE_EYE_LOWER_EYELID] = 0.15f
            liteMap[TYPE_BEAUTY_RESHAPE_BRIGHTEN_EYE] = 0.5f
            liteMap[TYPE_BEAUTY_RESHAPE_REMOVE_POUCH] = 0.5f

            // {zh} 鼻子 {en} Nose
            liteMap[TYPE_BEAUTY_RESHAPE_NOSE_SWING] = 0.7f

            // {zh} 嘴巴 {en} Mouth
            liteMap[TYPE_BEAUTY_RESHAPE_MOUTH_ZOOM] = 0.65f
            liteMap[TYPE_BEAUTY_RESHAPE_WHITEN_TEETH] = 0.35f
            //   {zh} 美体       {en} Body
            liteMap[TYPE_BEAUTY_BODY_ENHANCE_HIP] = 0.5f

            //   {zh} 美妆       {en} Beauty makeup
            liteMap[TYPE_MAKEUP_LIP] = 0.5f
            liteMap[TYPE_MAKEUP_HAIR] = 0.5f
            liteMap[TYPE_MAKEUP_BLUSHER] = 0.2f
            liteMap[TYPE_MAKEUP_FACIAL] = 0.35f
            liteMap[TYPE_MAKEUP_EYEBROW] = 0.35f
            liteMap[TYPE_MAKEUP_EYESHADOW] = 0.35f
            liteMap[TYPE_MAKEUP_PUPIL] = 0.4f
            liteMap[TYPE_MAKEUP_EYELASH] = 0.4f
            liteMap[TYPE_MAKEUP_EYELIGHT] = 0.4f
            liteMap[TYPE_MAKEUP_WOCAN] = 0.35f

            //   {zh} 风格妆       {en} Style makeup
            liteMap[TYPE_STYLE_MAKEUP] = floatArrayOf(0.8f, 0.8f)
            liteMap[TYPE_STYLE_MAKEUP_2D] = floatArrayOf(0.8f, 0.8f)
            liteMap[TYPE_STYLE_MAKEUP_3D] = floatArrayOf(0.8f, 0.8f)

            //   {zh} 滤镜       {en} Filter
            // filter
            liteMap[TYPE_FILTER] = 0.8f
            DEFAULT_LITE_VALUE = Collections.unmodifiableMap(liteMap)
        }

        init {
            colorForChooseMap[TYPE_MAKEUP_LIP] = ArrayList(
                listOf(
                    ColorItem(R.string.lip_color_yuanqi, 0.867f, 0.388f, 0.388f),
                    ColorItem(R.string.lip_color_rouhefen, 0.949f, 0.576f, 0.620f),
                    ColorItem(R.string.lip_color_xiyou, 0.945f, 0.510f, 0.408f),
                    ColorItem(R.string.lip_color_huolongguo, 0.714f, 0.224f, 0.388f),
                    ColorItem(R.string.lip_color_caomei, 0.631f, 0.016f, 0.016f)
                )
            )
            colorForChooseMap[TYPE_MAKEUP_BLUSHER] = ArrayList(
                listOf(
                    ColorItem(R.string.blusher_color_qianfen, 0.988f, 0.678f, 0.733f),
                    ColorItem(R.string.blusher_color_xingren, 0.996f, 0.796f, 0.545f),
                    ColorItem(R.string.blusher_color_shanhu, 1.000f, 0.565f, 0.443f),
                    ColorItem(R.string.blusher_color_fentao, 1.000f, 0.506f, 0.529f),
                    ColorItem(R.string.blusher_color_qianzi, 0.980f, 0.722f, 0.855f)
                )
            )
            colorForChooseMap[TYPE_MAKEUP_EYEBROW] = ArrayList(
                listOf(
                    ColorItem(R.string.black, 0.078f, 0.039f, 0.039f),
                    ColorItem(R.string.zong, 0.420f, 0.314f, 0.239f)
                )
            )
            colorForChooseMap[TYPE_MAKEUP_EYELASH] = ArrayList(
                listOf(
                    ColorItem(R.string.black, 0.078f, 0.039f, 0.039f),
                    ColorItem(R.string.zong, 0.420f, 0.314f, 0.239f)
                )
            )
            colorForChooseMap[TYPE_HAIR_DYE_HIGHLIGHT] = ArrayList(
                listOf(
                    ColorItem(R.string.hair_dye_blue_haze, 0.541f, 0.616f, 0.706f),
                    ColorItem(R.string.hair_dye_foggy_gray, 0.808f, 0.792f, 0.745f),
                    ColorItem(R.string.hair_dye_rose_red, 0.384f, 0.075f, 0.086f)
                )
            )
        }

        init {
            DefaultLiteEffects.add(TYPE_BEAUTY_FACE_SMOOTH)
            DefaultLiteEffects.add(TYPE_BEAUTY_FACE_WHITEN)
            DefaultLiteEffects.add(TYPE_BEAUTY_FACE_SHARPEN)
            DefaultLiteEffects.add(TYPE_BEAUTY_RESHAPE_FACE_OVERALL)
            DefaultLiteEffects.add(TYPE_BEAUTY_RESHAPE_EYE_SIZE)
        }

        init {
            DefaultStandardEffects.add(TYPE_BEAUTY_FACE_SMOOTH)
            DefaultStandardEffects.add(TYPE_BEAUTY_FACE_WHITEN)
            DefaultStandardEffects.add(TYPE_BEAUTY_FACE_SHARPEN)
            DefaultStandardEffects.add(TYPE_BEAUTY_RESHAPE_FACE_OVERALL)
            DefaultStandardEffects.add(TYPE_BEAUTY_RESHAPE_EYE_SIZE)
        }
    }

    private fun isDefaultEffect(type: Int): Boolean {
        return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
            DefaultLiteEffects.contains(type)
        } else {
            DefaultStandardEffects.contains(type)
        }
    }

    fun getItem(type: Int): ButtonItem? {
        var item = mSavedItems[type]
        if (item != null) {
            return item
        }
        when (type and MASK) {
            TYPE_BEAUTY_FACE -> item = beautyFaceItems
            TYPE_MAKEUP -> item = makeupItems
        }
        if (item != null) {
            mSavedItems[type] = item
        }
        return item
    }

    fun allItems(): List<ButtonItem> {
        val items: MutableList<ButtonItem> = ArrayList()
        for ((_, value) in mSavedItems) {
            items.add(value)
        }
        return items
    }

    private val beautyFaceItemsLite: ButtonItem
        get() {
            val beautyNode = beautyNode(mEffectType)
            val items = ArrayList<ButtonItem>()
            items.add(ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close))
            items.add(
                ButtonItem(
                    TYPE_BEAUTY_FACE_SMOOTH,
                    R.drawable.ic_beauty_smooth,
                    R.string.beauty_face_smooth,
                    ComposerNode(
                        beautyNode, "Smooth_ALL", getDefaultIntensity(
                            TYPE_BEAUTY_FACE_SMOOTH, mEffectType
                        )[0]
                    )
                )
            )
            if (hasWhiten()) {
                items.add(
                    ButtonItem(
                        TYPE_BEAUTY_FACE_WHITEN,
                        R.drawable.ic_beauty_whiten,
                        R.string.beauty_face_whiten,
                        ComposerNode(
                            beautyNode, "Foundation_ALL", getDefaultIntensity(
                                TYPE_BEAUTY_FACE_WHITEN, mEffectType
                            )[0]
                        )
                    )
                )
            }
            items.add(
                ButtonItem(
                    TYPE_BEAUTY_FACE_SHARPEN,
                    R.drawable.ic_beauty_sharpen,
                    R.string.beauty_face_sharpen,
                    ComposerNode(
                        beautyNode, "sharp", getDefaultIntensity(
                            TYPE_BEAUTY_FACE_SHARPEN, mEffectType
                        )[0]
                    )
                )
            )
            return ButtonItem(TYPE_BEAUTY_FACE, items)
        }
    private val beautyFaceItemsStandard: ButtonItem
        private get() {
            val beautyNode = beautyNode(mEffectType)
            val items = ArrayList<ButtonItem>()
            items.add(ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close))
            items.add(
                ButtonItem(
                    TYPE_BEAUTY_FACE_SMOOTH,
                    R.drawable.ic_beauty_smooth,
                    R.string.beauty_face_smooth,
                    ComposerNode(
                        "beauty_face_tob", "smooth", getDefaultIntensity(
                            TYPE_BEAUTY_FACE_SMOOTH, mEffectType
                        )[0]
                    )
                )
            )
            if (hasWhiten()) {
                items.add(
                    ButtonItem(
                        TYPE_BEAUTY_FACE_WHITEN,
                        R.drawable.ic_beauty_whiten,
                        R.string.beauty_face_whiten,
                        ComposerNode(
                            "beauty_face_tob", "whiten", getDefaultIntensity(
                                TYPE_BEAUTY_FACE_WHITEN, mEffectType
                            )[0]
                        )
                    )
                )
            }
            items.add(
                ButtonItem(
                    TYPE_BEAUTY_FACE_SHARPEN,
                    R.drawable.ic_beauty_sharpen,
                    R.string.beauty_face_sharpen,
                    ComposerNode(
                        beautyNode, "sharp", getDefaultIntensity(
                            TYPE_BEAUTY_FACE_SHARPEN, mEffectType
                        )[0]
                    )
                )
            )
            return ButtonItem(TYPE_BEAUTY_FACE, items)
        }

    private fun hasWhiten(): Boolean {
        return mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.STANDARD_ASIA
    }

    private fun hasDoubleEyeLip(): Boolean {
        return mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.STANDARD_ASIA
    }

    private val beautyFaceItems: ButtonItem?
        get() {
            when (mEffectType) {
                EffectType.LITE_ASIA, EffectType.LITE_NOT_ASIA -> return beautyFaceItemsLite
                EffectType.STANDARD_ASIA, EffectType.STANDARD_NOT_ASIA -> return beautyFaceItemsStandard
                else -> {}
            }
            return null
        }

    private val makeupItems: ButtonItem
        get() = if (mEffectType == EffectType.LITE_ASIA) {
            ButtonItem(
                TYPE_MAKEUP, listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.makeup_lip,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_LIP
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.makeup_blusher,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_BLUSHER
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.makeup_facial,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_FACIAL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.makeup_eyebrow,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYEBROW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.makeup_eye,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYESHADOW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.makeup_pupil,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_PUPIL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_eye_wocan,
                        R.string.makeup_wocan,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_WOCAN
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_HAIR,
                        R.drawable.ic_makeup_hair,
                        R.string.makeup_hair,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_HAIR
                        ),
                        false
                    )
                )
            )
        } else if (mEffectType == EffectType.LITE_NOT_ASIA) {
            ButtonItem(
                TYPE_MAKEUP,
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.makeup_lip,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_LIP
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.makeup_blusher,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_BLUSHER
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.makeup_facial,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_FACIAL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.makeup_eyebrow,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYEBROW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.makeup_eye,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYESHADOW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.makeup_pupil,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_PUPIL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_HAIR,
                        R.drawable.ic_makeup_hair,
                        R.string.makeup_hair,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_HAIR
                        ),
                        false
                    )
                )
            )
        } else if (mEffectType == EffectType.STANDARD_ASIA) {
            ButtonItem(
                TYPE_MAKEUP,
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.makeup_lip,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_LIP
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.makeup_blusher,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_BLUSHER
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.makeup_facial,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_FACIAL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.makeup_eyebrow,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYEBROW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.makeup_eye,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYESHADOW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELASH,
                        R.drawable.ic_makeup_eyelash,
                        R.string.makeup_eyelash,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYELASH
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELIGHT,
                        R.drawable.ic_makeup_eye_light,
                        R.string.makeup_eyelight,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYELIGHT
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.makeup_pupil,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_PUPIL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_eye_wocan,
                        R.string.makeup_wocan,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_WOCAN
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_HAIR,
                        R.drawable.ic_makeup_hair,
                        R.string.makeup_hair,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_HAIR
                        ),
                        false
                    )
                )
            )
        } else {
            ButtonItem(
                TYPE_MAKEUP,
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.makeup_lip,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_LIP
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.makeup_blusher,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_BLUSHER
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.makeup_facial,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_FACIAL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.makeup_eyebrow,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYEBROW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.makeup_eye,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYESHADOW
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELASH,
                        R.drawable.ic_makeup_eyelash,
                        R.string.makeup_eyelash,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYELASH
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELIGHT,
                        R.drawable.ic_makeup_eye_light,
                        R.string.makeup_eyelight,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_EYELIGHT
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.makeup_pupil,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_PUPIL
                        ),
                        false
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_HAIR,
                        R.drawable.ic_makeup_hair,
                        R.string.makeup_hair,
                        getMakeupOptionItems(
                            TYPE_MAKEUP_HAIR
                        ),
                        false
                    )
                )
            )
        }

    private fun getMakeupOptionItems(type: Int): List<ButtonItem> {
        when (type and SUB_MASK) {
            TYPE_MAKEUP_LIP -> return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_fuguhong,
                        ComposerNode(
                            "lip/lite/fuguhong", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_shaonvfen,
                        ComposerNode(
                            "lip/lite/shaonvfen", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_yuanqiju,
                        ComposerNode(
                            "lip/lite/yuanqiju", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_xiyouse,
                        ComposerNode(
                            "lip/lite/xiyouse", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_xiguahong,
                        ComposerNode(
                            "lip/lite/xiguahong", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_sironghong,
                        ComposerNode(
                            "lip/lite/sironghong", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_zangjuse,
                        ComposerNode(
                            "lip/lite/zangjuse", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_meizise,
                        ComposerNode(
                            "lip/lite/meizise", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_shanhuse,
                        ComposerNode(
                            "lip/lite/shanhuse", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_doushafen,
                        ComposerNode(
                            "lip/lite/doushafen", "Internal_Makeup_Lips", getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        )
                    )
                )
            } else {
                val colorItems: List<ColorItem> = getColorForChoose(mEffectType, TYPE_MAKEUP_LIP)
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_liangze,
                        ComposerNode(
                            "lip/standard/liangze",
                            arrayOf("Internal_Makeup_Lips", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_wumian,
                        ComposerNode(
                            "lip/standard/wumian",
                            arrayOf("Internal_Makeup_Lips", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_yaochun,
                        ComposerNode(
                            "lip/standard/yaochun",
                            arrayOf("Internal_Makeup_Lips", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_LIP,
                        R.drawable.ic_makeup_lip,
                        R.string.lip_yunruan,
                        ComposerNode(
                            "lip/standard/yunran",
                            arrayOf("Internal_Makeup_Lips", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_LIP, null
                            )[0]
                        ),
                        colorItems
                    )
                )
            }
            TYPE_MAKEUP_BLUSHER -> return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_weixunfen,
                        ComposerNode(
                            "blush/lite/weixun", "Internal_Makeup_Blusher", getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_richang,
                        ComposerNode(
                            "blush/lite/richang", "Internal_Makeup_Blusher", getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_mitao,
                        ComposerNode(
                            "blush/lite/mitao", "Internal_Makeup_Blusher", getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_tiancheng,
                        ComposerNode(
                            "blush/lite/tiancheng", "Internal_Makeup_Blusher", getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_qiaopi,
                        ComposerNode(
                            "blush/lite/qiaopi", "Internal_Makeup_Blusher", getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_xinji,
                        ComposerNode(
                            "blush/lite/xinji", "Internal_Makeup_Blusher", getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_shaishang,
                        ComposerNode(
                            "blush/lite/shaishang", "Internal_Makeup_Blusher", getDefaultIntensity(
                                TYPE_BEAUTY_RESHAPE_EYE, null
                            )[0]
                        )
                    )
                )
            } else {
                val colorItems = getColorForChoose(mEffectType, TYPE_MAKEUP_BLUSHER)
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_mitao,
                        ComposerNode(
                            "blush/standard/mitao",
                            arrayOf("Internal_Makeup_Blusher", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_weixun,
                        ComposerNode(
                            "blush/standard/weixun",
                            arrayOf("Internal_Makeup_Blusher", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_yuanqi,
                        ComposerNode(
                            "blush/standard/yuanqi",
                            arrayOf("Internal_Makeup_Blusher", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_qise,
                        ComposerNode(
                            "blush/standard/qise",
                            arrayOf("Internal_Makeup_Blusher", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_shaishang,
                        ComposerNode(
                            "blush/standard/shaishang",
                            arrayOf("Internal_Makeup_Blusher", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_rixi,
                        ComposerNode(
                            "blush/standard/rixi",
                            arrayOf("Internal_Makeup_Blusher", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_BLUSHER, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_BLUSHER,
                        R.drawable.ic_makeup_blusher,
                        R.string.blusher_suzui,
                        ComposerNode(
                            "blush/standard/suzui",
                            arrayOf("Internal_Makeup_Blusher", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_BEAUTY_RESHAPE_EYE, null
                            )[0]
                        ),
                        colorItems
                    )
                )
            }
            TYPE_MAKEUP_PUPIL -> return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_hunxuezong,
                        ComposerNode(
                            "pupil/hunxuezong", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_kekezong,
                        ComposerNode(
                            "pupil/kekezong", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_mitaofen,
                        ComposerNode(
                            "pupil/mitaofen", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_shuiguanghei,
                        ComposerNode(
                            "pupil/shuiguanghei", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_xingkonglan,
                        ComposerNode(
                            "pupil/xingkonglan", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_chujianhui,
                        ComposerNode(
                            "pupil/chujianhui", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    )
                )
            } else {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_yuansheng,
                        ComposerNode(
                            "pupil/yuansheng", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_xinxinzi,
                        ComposerNode(
                            "pupil/xinxinzi", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_huoxing,
                        ComposerNode(
                            "pupil/huoxing", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_huilv,
                        ComposerNode(
                            "pupil/huilv", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_PUPIL,
                        R.drawable.ic_makeup_pupil,
                        R.string.pupil_huitong,
                        ComposerNode(
                            "pupil/huitong", "Internal_Makeup_Pupil", getDefaultIntensity(
                                TYPE_MAKEUP_PUPIL, null
                            )[0]
                        )
                    )
                )
            }
            TYPE_MAKEUP_HAIR -> return listOf(
                ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                ButtonItem(
                    TYPE_MAKEUP_HAIR,
                    R.drawable.ic_makeup_hair,
                    R.string.hair_anlan,
                    ComposerNode("hair/anlan")
                ),
                ButtonItem(
                    TYPE_MAKEUP_HAIR,
                    R.drawable.ic_makeup_hair,
                    R.string.hair_molv,
                    ComposerNode("hair/molv")
                ),
                ButtonItem(
                    TYPE_MAKEUP_HAIR,
                    R.drawable.ic_makeup_hair,
                    R.string.hair_shenzong,
                    ComposerNode("hair/shenzong")
                )
            )
            TYPE_MAKEUP_EYESHADOW -> return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_wanxiahong,
                        ComposerNode(
                            "eyeshadow/wanxiahong", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_shaonvfen,
                        ComposerNode(
                            "eyeshadow/shaonvfen", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_qizhifen,
                        ComposerNode(
                            "eyeshadow/qizhifen", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_meizihong,
                        ComposerNode(
                            "eyeshadow/meizihong", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_jiaotangzong,
                        ComposerNode(
                            "eyeshadow/jiaotangzong", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_yuanqiju,
                        ComposerNode(
                            "eyeshadow/yuanqiju", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_naichase,
                        ComposerNode(
                            "eyeshadow/naichase", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    )
                )
            } else {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_dadizong,
                        ComposerNode(
                            "eyeshadow/dadizong", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_hanxi,
                        ComposerNode(
                            "eyeshadow/hanxi", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_nvshen,
                        ComposerNode(
                            "eyeshadow/nvshen", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_xingzi,
                        ComposerNode(
                            "eyeshadow/xingzi", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYESHADOW,
                        R.drawable.ic_makeup_eyeshadow,
                        R.string.eye_bingtang,
                        ComposerNode(
                            "eyeshadow/bingtangshanzha", "Internal_Makeup_Eye", getDefaultIntensity(
                                TYPE_MAKEUP_EYESHADOW, null
                            )[0]
                        )
                    )
                )
            }
            TYPE_MAKEUP_EYEBROW -> return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_zongse,
                        ComposerNode(
                            "eyebrow/lite/BR01", "Internal_Makeup_Brow", getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_cuhei,
                        ComposerNode(
                            "eyebrow/lite/BK01", "Internal_Makeup_Brow", getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_heise,
                        ComposerNode(
                            "eyebrow/lite/BK02", "Internal_Makeup_Brow", getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_xihei,
                        ComposerNode(
                            "eyebrow/lite/BK03", "Internal_Makeup_Brow", getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        )
                    )
                )
            } else {
                val colorItems = getColorForChoose(mEffectType, TYPE_MAKEUP_EYEBROW)
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_biaozhun,
                        ComposerNode(
                            "eyebrow/standard/biaozhun",
                            arrayOf("Internal_Makeup_Brow", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_liuye,
                        ComposerNode(
                            "eyebrow/standard/liuye",
                            arrayOf("Internal_Makeup_Brow", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_rongrong,
                        ComposerNode(
                            "eyebrow/standard/rongrong",
                            arrayOf("Internal_Makeup_Brow", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYEBROW,
                        R.drawable.ic_makeup_eyebrow,
                        R.string.eyebrow_yesheng,
                        ComposerNode(
                            "eyebrow/standard/yesheng",
                            arrayOf("Internal_Makeup_Brow", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYEBROW, null
                            )[0]
                        ),
                        colorItems
                    )
                )
            }
            TYPE_MAKEUP_FACIAL -> return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.facial_jingzhi,
                        ComposerNode(
                            "facial/jingzhi", "Internal_Makeup_Facial", getDefaultIntensity(
                                TYPE_MAKEUP_FACIAL, null
                            )[0]
                        )
                    )
                )
            } else {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.facial_ziran,
                        ComposerNode(
                            "facial/ziran", "Internal_Makeup_Facial", getDefaultIntensity(
                                TYPE_MAKEUP_FACIAL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.facial_xiaov,
                        ComposerNode(
                            "facial/xiaov", "Internal_Makeup_Facial", getDefaultIntensity(
                                TYPE_MAKEUP_FACIAL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.facial_fajixian,
                        ComposerNode(
                            "facial/fajixian", "Internal_Makeup_Facial", getDefaultIntensity(
                                TYPE_MAKEUP_FACIAL, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_FACIAL,
                        R.drawable.ic_makeup_facial,
                        R.string.facial_gaoguang,
                        ComposerNode(
                            "facial/gaoguang", "Internal_Makeup_Facial", getDefaultIntensity(
                                TYPE_MAKEUP_FACIAL, null
                            )[0]
                        )
                    )
                )
            }
            TYPE_MAKEUP_EYELASH -> {
                val colorItems = getColorForChoose(mEffectType, TYPE_MAKEUP_EYELASH)
                return listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_EYELASH,
                        R.drawable.ic_makeup_eyelash,
                        R.string.eyelash_ziran,
                        ComposerNode(
                            "eyelashes/ziran",
                            arrayOf("Internal_Makeup_Eyelash", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYELASH, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELASH,
                        R.drawable.ic_makeup_eyelash,
                        R.string.eyelash_juanqiao,
                        ComposerNode(
                            "eyelashes/juanqiao",
                            arrayOf("Internal_Makeup_Eyelash", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYELASH, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELASH,
                        R.drawable.ic_makeup_eyelash,
                        R.string.eyelash_chibang,
                        ComposerNode(
                            "eyelashes/chibang",
                            arrayOf("Internal_Makeup_Eyelash", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYELASH, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELASH,
                        R.drawable.ic_makeup_eyelash,
                        R.string.eyelash_manhua,
                        ComposerNode(
                            "eyelashes/manhua",
                            arrayOf("Internal_Makeup_Eyelash", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYELASH, null
                            )[0]
                        ),
                        colorItems
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_EYELASH,
                        R.drawable.ic_makeup_eyelash,
                        R.string.eyelash_xiachui,
                        ComposerNode(
                            "eyelashes/xiachui",
                            arrayOf("Internal_Makeup_Eyelash", "R", "G", "B"),
                            getDefaultIntensity(
                                TYPE_MAKEUP_EYELASH, null
                            )[0]
                        ),
                        colorItems
                    )
                )
            }
            TYPE_MAKEUP_WOCAN -> return if (mEffectType == EffectType.LITE_ASIA || mEffectType == EffectType.LITE_NOT_ASIA) {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_wocan,
                        R.string.wocan_ziran,
                        ComposerNode(
                            "wocan/ziran", "Internal_Makeup_WoCan", getDefaultIntensity(
                                TYPE_MAKEUP_WOCAN, null
                            )[0]
                        )
                    )
                )
            } else {
                listOf(
                    ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_wocan,
                        R.string.wocan_suyan,
                        ComposerNode(
                            "wocan/suyan", "Internal_Makeup_WoCan", getDefaultIntensity(
                                TYPE_MAKEUP_WOCAN, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_wocan,
                        R.string.wocan_chulian,
                        ComposerNode(
                            "wocan/chulian", "Internal_Makeup_WoCan", getDefaultIntensity(
                                TYPE_MAKEUP_WOCAN, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_wocan,
                        R.string.wocan_manhuayan,
                        ComposerNode(
                            "wocan/manhuayan", "Internal_Makeup_WoCan", getDefaultIntensity(
                                TYPE_MAKEUP_WOCAN, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_wocan,
                        R.string.wocan_xiachui,
                        ComposerNode(
                            "wocan/xiachui", "Internal_Makeup_WoCan", getDefaultIntensity(
                                TYPE_MAKEUP_WOCAN, null
                            )[0]
                        )
                    ),
                    ButtonItem(
                        TYPE_MAKEUP_WOCAN,
                        R.drawable.ic_makeup_wocan,
                        R.string.wocan_taohua,
                        ComposerNode(
                            "wocan/taohua", "Internal_Makeup_WoCan", getDefaultIntensity(
                                TYPE_MAKEUP_WOCAN, null
                            )[0]
                        )
                    )
                )
            }
            TYPE_MAKEUP_EYELIGHT -> return listOf(
                ButtonItem(TYPE_CLOSE, R.drawable.clear_no_border, R.string.close),
                ButtonItem(
                    TYPE_MAKEUP_EYELIGHT,
                    R.drawable.ic_makeup_eye_light,
                    R.string.makeup_eyelight_ziranguang,
                    ComposerNode(
                        "eyelight/ziranguang", "Internal_Makeup_EyeLight", getDefaultIntensity(
                            TYPE_MAKEUP_EYELIGHT, null
                        )[0]
                    )
                ),
                ButtonItem(
                    TYPE_MAKEUP_EYELIGHT,
                    R.drawable.ic_makeup_eye_light,
                    R.string.makeup_eyelight_yueyaguang,
                    ComposerNode(
                        "eyelight/yueyaguang", "Internal_Makeup_EyeLight", getDefaultIntensity(
                            TYPE_MAKEUP_EYELIGHT, null
                        )[0]
                    )
                ),
                ButtonItem(
                    TYPE_MAKEUP_EYELIGHT,
                    R.drawable.ic_makeup_eye_light,
                    R.string.makeup_eyelight_juguangdeng,
                    ComposerNode(
                        "eyelight/juguangdeng", "Internal_Makeup_EyeLight", getDefaultIntensity(
                            TYPE_MAKEUP_EYELIGHT, null
                        )[0]
                    )
                )
            )
        }
        return listOf()
    }

    private fun beautyNode(mEffectType: EffectType): String {
        return if (mEffectType == EffectType.STANDARD_ASIA || mEffectType == EffectType.STANDARD_NOT_ASIA) NODE_BEAUTY_STANDARD else NODE_BEAUTY_LITE
    }
}