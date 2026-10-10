package com.example.nkust_app.data.campus

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.FireExtinguisher
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlutterDash
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Skateboarding
import androidx.compose.material.icons.filled.SportsBaseball
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.SportsVolleyball
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Waves
import androidx.compose.ui.graphics.Color

// ⚠️ 這個檔案由 iOS 版 Campus/*MapView.swift 自動轉換產生。
// 要新增或修改建築、教室，直接改下面的清單即可。

// iOS 系統色（地圖標記用）
val MapTeal = Color(0xFF30B0C7)
val MapBlue = Color(0xFF007AFF)
val MapBrown = Color(0xFFA2845E)
val MapGray = Color(0xFF8E8E93)
val MapGreen = Color(0xFF34C759)
val MapOrange = Color(0xFFFF9500)
val MapPink = Color(0xFFFF2D55)
val MapPurple = Color(0xFFAF52DE)
val MapYellow = Color(0xFFFFCC00)

/** 由 iOS 的 JiangongMapView.swift 自動轉換 */
val JiangongBuildings: List<Building> = listOf(
    Building(
        name = "正門",
        latitude = 22.651919,
        longitude = 120.328684,
        description = "建工校區正門口",
        icon = Icons.Filled.DoorFront,
        color = MapTeal
    ),
    Building(
        name = "收發室",
        latitude = 22.651725905758113,
        longitude = 120.32890160437056,
        description = "",
        icon = Icons.Filled.Home,
        color = MapGray
    ),
    Building(
        name = "機車棚",
        latitude = 22.651596922203588,
        longitude = 120.32908811277623,
        description = "東區機車棚",
        icon = Icons.Filled.TwoWheeler,
        color = MapPink
    ),
    Building(
        name = "機車棚",
        latitude = 22.651248,
        longitude = 120.327595,
        description = "西區機車棚",
        icon = Icons.Filled.TwoWheeler,
        color = MapPink
    ),
    Building(
        name = "汽車停車場",
        latitude = 22.650755,
        longitude = 120.327793,
        description = "",
        icon = Icons.Filled.DirectionsCar,
        color = MapPink
    ),
    Building(
        name = "行政大樓",
        latitude = 22.651327,
        longitude = 120.328770,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapBlue,
        classrooms = listOf(
            Floor("4F", listOf("行401", "行402", "行403", "行404", "行405多功能研習", "行406", "行407", "行408", "行409", "行410")),
            Floor("5F", listOf("行501", "行502", "行503", "行504", "行505AI", "行506", "行507", "行508", "行509", "行510", "行511"))
        )
    ),
    Building(
        name = "育賢樓",
        latitude = 22.650496705338718,
        longitude = 120.32777429929709,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapOrange,
        classrooms = listOf(
            Floor("1F", listOf("育101", "育102", "育103", "育104", "育105")),
            Floor("2F", listOf("育201", "育202", "育203", "育204", "育205", "育206")),
            Floor("3F", listOf("育301", "育302", "育303", "育304", "育305")),
            Floor("4F", listOf("育400", "育401", "育402", "育403", "育404", "育405")),
            Floor("5F", listOf("育500", "育501", "育502", "育503", "育505"))
        )
    ),
    Building(
        name = "圖書館",
        latitude = 22.650189621341763,
        longitude = 120.32779032327883,
        description = "",
        icon = Icons.Filled.Book,
        color = MapBlue,
        classrooms = listOf(
            Floor("1F", listOf("資訊工業大樓 1F")),
            Floor("2F", listOf("資訊工業大樓 2F")),
            Floor("3F", listOf("資訊工業大樓 3F"))
        )
    ),
    Building(
        name = "資訊工業大樓",
        latitude = 22.650131760283124,
        longitude = 120.32768804576807,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("資001", "資002")),
            Floor("4F", listOf("資401", "資405", "資406B")),
            Floor("5F", listOf("資501A", "資504")),
            Floor("6F", listOf("資606")),
            Floor("7F", listOf("資701", "資704")),
            Floor("8F", listOf("資805", "資809", "系辦")),
            Floor("10F", listOf("光電所第一研討室", "光電所第二研討室")),
            Floor("12F", listOf("資1207"))
        )
    ),
    Building(
        name = "咖啡廣場",
        latitude = 22.650119,
        longitude = 120.328045,
        description = "",
        icon = Icons.Filled.LocalCafe,
        color = MapBrown
    ),
    Building(
        name = "學生餐廳",
        latitude = 22.649723,
        longitude = 120.327710,
        description = "",
        icon = Icons.Filled.Restaurant,
        color = MapOrange
    ),
    Building(
        name = "學生活動中心",
        latitude = 22.649929,
        longitude = 120.327944,
        description = "",
        icon = Icons.Filled.SportsTennis,
        color = MapOrange,
        classrooms = listOf(
            Floor("B1F", listOf("健身房", "桌球教室", "韻律教室", "多功能訓練室")),
            Floor("1F", listOf("藝術文化中心展覽廳", "圖書文具部")),
            Floor("2F", listOf("藝術文化中心表演廳", "學生會行政會議室", "學生會行政辦公室")),
            Floor("3F", listOf("活動中心 G302", "視聽教室", "羽球場", "體育器材室")),
            Floor("4F", listOf("體育教師研究室", "羽球館看臺")),
            Floor("5F", listOf("學生社團辦公室(體能性社團)")),
            Floor("6F", listOf("學生社團辦公室(系學會、康樂性社團)")),
            Floor("7F", listOf("學生議會辦公室", "學生社團辦公室(服務性社團、學藝性社團)"))
        )
    ),
    Building(
        name = "ATM",
        latitude = 22.649769,
        longitude = 120.327980,
        description = "",
        icon = Icons.Filled.AccountBalance,
        color = MapGreen
    ),
    Building(
        name = "教學大樓",
        latitude = 22.650743,
        longitude = 120.328742,
        description = "無資訊",
        icon = Icons.Filled.Apartment,
        color = MapBlue
    ),
    Building(
        name = "西棟",
        latitude = 22.650581872973152,
        longitude = 120.32833808217707,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapBlue,
        classrooms = listOf(
            Floor("B1F", listOf("西001", "西002", "西003", "西004", "西005")),
            Floor("1F", listOf("西101", "西102", "西103", "西104")),
            Floor("3F", listOf("西302C"))
        )
    ),
    Building(
        name = "東棟",
        latitude = 22.650566872946502,
        longitude = 120.32920356354421,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapBlue,
        classrooms = listOf(
            Floor("B1F", listOf("東001", "東002", "東003", "東004", "東005")),
            Floor("1F", listOf("東104", "東105")),
            Floor("2F", listOf("東201", "東203")),
            Floor("3F", listOf("東304", "東305"))
        )
    ),
    Building(
        name = "萊爾富",
        latitude = 22.649755037673355,
        longitude = 120.32901559874364,
        description = "",
        icon = Icons.Filled.Storefront,
        color = MapGreen
    ),
    Building(
        name = "電機館",
        latitude = 22.64947532320891,
        longitude = 120.32855157660838,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("電B02", "電B05", "電B06", "電小劇場")),
            Floor("1F", listOf("電105", "電106", "慧芳講堂")),
            Floor("4F", listOf("電401")),
            Floor("5F", listOf("電503", "電504", "電505", "電506")),
            Floor("6F", listOf("電601", "電603"))
        )
    ),
    Building(
        name = "模具館",
        latitude = 22.64947532320891,
        longitude = 120.32913250623187,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("模103", "模111")),
            Floor("2F", listOf("模205", "模208", "模210")),
            Floor("3F", listOf("模308")),
            Floor("5F", listOf("模501", "模505", "模510", "模514")),
            Floor("6F", listOf("模601", "模603", "模605"))
        )
    ),
    Building(
        name = "土木工程館",
        latitude = 22.648918902987248,
        longitude = 120.32911384274722,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("土101", "土107")),
            Floor("2F", listOf("土207")),
            Floor("3F", listOf("土302A", "土308", "土309")),
            Floor("4F", listOf("土401A", "土408", "土409")),
            Floor("5F", listOf("土502F", "土503", "土508", "土509")),
            Floor("6F", listOf("土608", "土609")),
            Floor("7F", listOf("土702", "土703", "土708", "土709"))
        )
    ),
    Building(
        name = "機械工程館",
        latitude = 22.648282093812334,
        longitude = 120.3291390157567,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("機B02")),
            Floor("1F", listOf("機101A", "機102", "機103", "M103A", "材料實驗室")),
            Floor("2F", listOf("機203", "機204")),
            Floor("3F", listOf("機304", "機306", "機307")),
            Floor("4F", listOf("機402", "機403", "機404", "機407", "機408", "機409"))
        )
    ),
    Building(
        name = "中正堂",
        latitude = 22.64928426315465,
        longitude = 120.32790094610567,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "化材館",
        latitude = 22.64897979412971,
        longitude = 120.32770782706619,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("化材B1-07(化001)")),
            Floor("1F", listOf("曉東講堂")),
            Floor("3F", listOf("化305")),
            Floor("4F", listOf("化402")),
            Floor("8F", listOf("化809", "化812"))
        )
    ),
    Building(
        name = "多功能實習工廠",
        latitude = 22.649233,
        longitude = 120.327644,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("多101")),
            Floor("2F", listOf("多201")),
            Floor("4F", listOf("多401")),
            Floor("5F", listOf("多502"))
        )
    ),
    Building(
        name = "司令台",
        latitude = 22.648251466414806,
        longitude = 120.32783600364664,
        description = "",
        icon = Icons.Filled.Campaign,
        color = MapGray,
        classrooms = listOf(
            Floor("2F", listOf("司207", "司208"))
        )
    ),
    Building(
        name = "排球場",
        latitude = 22.647762603314884,
        longitude = 120.32913901575833,
        description = "",
        icon = Icons.Filled.SportsVolleyball,
        color = MapYellow
    ),
    Building(
        name = "網球場",
        latitude = 22.647426513573453,
        longitude = 120.32915235114201,
        description = "",
        icon = Icons.Filled.SportsTennis,
        color = MapGreen
    ),
    Building(
        name = "游泳池",
        latitude = 22.647245178534902,
        longitude = 120.32880030348547,
        description = "",
        icon = Icons.Filled.Pool,
        color = MapBlue
    ),
    Building(
        name = "慧樓(女宿)",
        latitude = 22.647255961421443,
        longitude = 120.32863679429775,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapPink
    ),
    Building(
        name = "影印&訂書",
        latitude = 22.647225545673837,
        longitude = 120.32827403825472,
        description = "",
        icon = Icons.Filled.Print,
        color = MapGray
    ),
    Building(
        name = "男宿舍",
        latitude = 22.646826662914165,
        longitude = 120.328746,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapBlue
    ),
    Building(
        name = "後門",
        latitude = 22.64674497470727,
        longitude = 120.32930942294551,
        description = "",
        icon = Icons.Filled.DoorFront,
        color = MapTeal
    )
)

/** 由 iOS 的 YanchaoMapView.swift 自動轉換 */
val YanchaoBuildings: List<Building> = listOf(
    Building(
        name = "校門口",
        latitude = 22.772462,
        longitude = 120.400273,
        description = "燕巢校區正門口",
        icon = Icons.Filled.DoorFront,
        color = MapTeal
    ),
    Building(
        name = "停車場",
        latitude = 22.77265513664243,
        longitude = 120.40063893346289,
        description = "",
        icon = Icons.Filled.LocalParking,
        color = MapBlue
    ),
    Building(
        name = "機車棚",
        latitude = 22.77265940878065,
        longitude = 120.39979807315984,
        description = "",
        icon = Icons.Filled.TwoWheeler,
        color = MapPink
    ),
    Building(
        name = "7-ELEVEN",
        latitude = 22.77247788989449,
        longitude = 120.39940685145297,
        description = "",
        icon = Icons.Filled.Storefront,
        color = MapGreen
    ),
    Building(
        name = "多功能球場",
        latitude = 22.77271801121566,
        longitude = 120.39882087080704,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "資源回收場",
        latitude = 22.7726570327192,
        longitude = 120.3979109268858,
        description = "",
        icon = Icons.Filled.Recycling,
        color = MapGray
    ),
    Building(
        name = "大學一橋",
        latitude = 22.773911264221315,
        longitude = 120.4006362012409,
        description = "",
        icon = Icons.Filled.Traffic,
        color = MapGray
    ),
    Building(
        name = "大學二橋",
        latitude = 22.77342617805246,
        longitude = 120.39820525481308,
        description = "",
        icon = Icons.Filled.Traffic,
        color = MapGray
    ),
    Building(
        name = "智善廳",
        latitude = 22.774018928739476,
        longitude = 120.3978573433828,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "創新設計學院(原人文社會學院)",
        latitude = 22.7742504476864,
        longitude = 120.39820307969825,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("2F", listOf("HS207", "HS209", "HS210", "HS221", "HS222")),
            Floor("3F", listOf("HS303", "HS304", "HS305", "HS313", "HS314", "HS315", "HS316", "HS319", "HS327", "HS328")),
            Floor("4F", listOf("HS405", "HS408", "HS414", "HS415", "HS416", "HS418", "HS427")),
            Floor("5F", listOf("HS507", "HS508", "HS509", "HS514", "HS516", "HS517", "HS519", "HS528", "HS529")),
            Floor("6F", listOf("HS603", "HS605", "HS608", "HS609"))
        )
    ),
    Building(
        name = "行政大樓",
        latitude = 22.77510880590793,
        longitude = 120.40129606110933,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1", listOf("停車場")),
            Floor("1F", listOf("行動辦公室", "衛保組", "健康中心", "哺乳室", "體育室", "綜合業務處處長辦公室", "綜合業務處聯合辦公室")),
            Floor("2F", listOf("財務室", "總務長室", "文書組檔案室", "主計室憑證室", "節能暨防災監控室")),
            Floor("3F", listOf("人事室", "主計室(第二、三、四組)")),
            Floor("4F", listOf("80人會議室", "45人會議室", "20人會議室", "165人會議室", "主計室(第一、五組)")),
            Floor("5F", listOf("校長室", "副校長室", "秘書室"))
        )
    ),
    Building(
        name = "圖書資訊大樓",
        latitude = 22.776103150056958,
        longitude = 120.4009617570848,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "景觀吊橋",
        latitude = 22.77525074940993,
        longitude = 120.40203024283728,
        description = "",
        icon = Icons.Filled.Hiking,
        color = MapGray
    ),
    Building(
        name = "樂知樓(男宿)",
        latitude = 22.775309169561297,
        longitude = 120.40297776790166,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapBlue
    ),
    Building(
        name = "詠絮樓",
        latitude = 22.77537008842355,
        longitude = 120.40352286039284,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "涵芳樓",
        latitude = 22.77514092585854,
        longitude = 120.40368470118092,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "開心農場",
        latitude = 22.774150,
        longitude = 120.403370,
        description = "",
        icon = Icons.Filled.SelfImprovement,
        color = MapGreen
    ),
    Building(
        name = "生態池",
        latitude = 22.77340738297478,
        longitude = 120.40090947946895,
        description = "",
        icon = Icons.Filled.FlutterDash,
        color = MapGreen
    ),
    Building(
        name = "商業智慧學院(原管院二館)",
        latitude = 22.773374229410397,
        longitude = 120.40204028313187,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1", listOf("MB002")),
            Floor("1F", listOf("MB101", "MB104", "MB105", "MB107", "MB108", "MB109", "MB110", "MB112", "MB113")),
            Floor("2F", listOf("MB201", "MB202", "MB207", "MB208", "MB209", "MB210")),
            Floor("3F", listOf("MB302", "MB303", "MB304", "MB322", "MB323", "MB324", "MB325")),
            Floor("4F", listOf("MB403A", "MB404", "MB422", "MB423", "MB424", "MB425A", "MB425B")),
            Floor("5F", listOf("MB503", "MB504", "MB522", "MB523", "MB524", "MB525A", "MB525B")),
            Floor("6F", listOf("MB602", "MB603"))
        )
    ),
    Building(
        name = "商業智慧學院(原管院一館)",
        latitude = 22.773523453519473,
        longitude = 120.40306334808744,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("MA104", "MA106", "MA108", "MA112", "MA114", "MA115")),
            Floor("2F", listOf("MA202", "MA203", "MA205", "MA206", "MA207", "MA208", "MA209", "MA211", "MA212", "MA213", "MA215", "MA216", "MA217B", "MA218")),
            Floor("3F", listOf("MA318", "MA319", "MA320", "MA321", "MA324", "MA325")),
            Floor("4F", listOf("MA409B", "MA410A", "MA421-AI", "MA423A", "MA423B")),
            Floor("5F", listOf("MA503", "MA504", "MA505"))
        )
    ),
    Building(
        name = "麗文書局",
        latitude = 22.773684371210333,
        longitude = 120.40212183787902,
        description = "",
        icon = Icons.Filled.Architecture,
        color = MapOrange
    )
)

/** 由 iOS 的 DiyiMapView.swift 自動轉換 */
val DiyiBuildings: List<Building> = listOf(
    Building(
        name = "西區校門口",
        latitude = 22.751763917724624,
        longitude = 120.33147661362545,
        description = "卓越路校門口",
        icon = Icons.Filled.DoorFront,
        color = MapTeal
    ),
    Building(
        name = "機車棚",
        latitude = 22.752431762124747,
        longitude = 120.33066122207738,
        description = "樂群樓機車停車場",
        icon = Icons.Filled.TwoWheeler,
        color = MapPink
    ),
    Building(
        name = "樂群樓(女宿)",
        latitude = 22.752570277591047,
        longitude = 120.3299102036017,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapPink
    ),
    Building(
        name = "產業創新園區",
        latitude = 22.75222627037895,
        longitude = 120.32855343476429,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("3F", listOf("H303"))
        )
    ),
    Building(
        name = "籃球場",
        latitude = 22.753324497945144,
        longitude = 120.3272391523377,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "溜冰場",
        latitude = 22.753403649138825,
        longitude = 120.32786678922146,
        description = "",
        icon = Icons.Filled.Skateboarding,
        color = MapBlue
    ),
    Building(
        name = "壘球場",
        latitude = 22.754415296307485,
        longitude = 120.32754224195054,
        description = "",
        icon = Icons.Filled.SportsBaseball,
        color = MapGreen
    ),
    Building(
        name = "運動場",
        latitude = 22.753764775883536,
        longitude = 120.32892357960179,
        description = "",
        icon = Icons.Filled.FitnessCenter,
        color = MapGreen
    ),
    Building(
        name = "籃球場",
        latitude = 22.75320824454967,
        longitude = 120.330060836223,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "網球場",
        latitude = 22.75409374667951,
        longitude = 120.33004206074511,
        description = "",
        icon = Icons.Filled.SportsTennis,
        color = MapGreen
    ),
    Building(
        name = "游泳池",
        latitude = 22.754397982119887,
        longitude = 120.33149581804564,
        description = "",
        icon = Icons.Filled.Pool,
        color = MapBlue
    ),
    Building(
        name = "敬業樓(男宿)",
        latitude = 22.7554516703778,
        longitude = 120.33182304755766,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapBlue
    ),
    Building(
        name = "機車棚",
        latitude = 22.75654739858988,
        longitude = 120.33169430150834,
        description = "",
        icon = Icons.Filled.TwoWheeler,
        color = MapPink
    ),
    Building(
        name = "籃球場",
        latitude = 22.75692335748664,
        longitude = 120.33193838252399,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "鴴池(生態池)",
        latitude = 22.753549584039096,
        longitude = 120.33146094931341,
        description = "",
        icon = Icons.Filled.FlutterDash,
        color = MapGreen
    ),
    Building(
        name = "大學橋",
        latitude = 22.75472154244634,
        longitude = 120.33362515890246,
        description = "",
        icon = Icons.Filled.Traffic,
        color = MapGray
    ),
    Building(
        name = "智慧防災實作工場",
        latitude = 22.753578567037376,
        longitude = 120.33529839904105,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("P102")),
            Floor("2F", listOf("P202")),
            Floor("3F", listOf("P302"))
        )
    ),
    Building(
        name = "南區毒災中心",
        latitude = 22.753889538387615,
        longitude = 120.33559288200689,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("Y105")),
            Floor("3F", listOf("Y311"))
        )
    ),
    Building(
        name = "學生活動中心",
        latitude = 22.754659396240474,
        longitude = 120.33540802136463,
        description = "7-ELEVEN 高科大門市 & 圖書文具部",
        icon = Icons.Filled.Storefront,
        color = MapOrange
    ),
    Building(
        name = "水舞廣場",
        latitude = 22.755061400722205,
        longitude = 120.33477956826505,
        description = "",
        icon = Icons.Filled.Waves,
        color = MapBlue
    ),
    Building(
        name = "圖書資訊大樓",
        latitude = 22.75571778487593,
        longitude = 120.33520079666971,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("J001", "J009", "實作區B", "木工區", "創夢工場電子區")),
            Floor("1F", listOf("J124"))
        )
    ),
    Building(
        name = "財經學院",
        latitude = 22.756202015890334,
        longitude = 120.33485596933536,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("E001", "E002", "E003", "E004", "E005", "E006", "E007", "E008", "E009", "E010", "E011", "E012", "E013", "E014", "E015", "E016")),
            Floor("1F", listOf("E101", "E102", "E103", "E104", "E105", "E106", "E107", "E109", "E110", "E113", "E115", "E116", "E117", "E118", "E119", "E120")),
            Floor("2F", listOf("E201", "E202", "E203", "E204", "E205", "E206", "E207", "E209", "E210", "E211", "E212", "E213", "E214", "E217", "E220")),
            Floor("3F", listOf("E301", "E302", "E303", "E304", "E305", "E306", "E307", "E309", "E310", "E311", "E312", "E313", "E314", "E315", "E317", "E318", "E319")),
            Floor("4F", listOf("E403", "E416", "E419", "E420", "E421", "E422", "E423", "E424", "E425")),
            Floor("5F", listOf("E514", "E525", "E534"))
        )
    ),
    Building(
        name = "外語學院",
        latitude = 22.756639615540866,
        longitude = 120.33615568367877,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("D009")),
            Floor("1F", listOf("D101", "D102多功能教室", "D103多功能教室", "D104", "D105", "D106", "D107", "D108", "D109", "D110電腦教室", "D111")),
            Floor("2F", listOf("D201", "D202電腦教室", "D203", "D204文化教室", "D205電腦教室", "D206A", "D206B", "D208電腦教室", "D209電腦教室", "綜合業務處")),
            Floor("3F", listOf("D301文化教室", "D302", "D308")),
            Floor("4F", listOf("D414", "D415", "D416", "D417", "D418", "D419電腦教室")),
            Floor("5F", listOf("D516", "D517"))
        )
    ),
    Building(
        name = "管理學院",
        latitude = 22.75714418518805,
        longitude = 120.33734636744386,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("C103", "C104", "C116", "C117", "C120", "C123實驗教室", "C124冷鏈實驗室", "C125")),
            Floor("2F", listOf("C203", "C204", "C218", "C219", "C220", "C222", "C228-4")),
            Floor("3F", listOf("C333", "C338", "C340", "C345", "C351會議室", "C356", "C359", "C363")),
            Floor("4F", listOf("C433", "C434", "C442", "C443", "C444", "C445", "C446", "C448"))
        )
    ),
    Building(
        name = "電資學院",
        latitude = 22.757768089545554,
        longitude = 120.3379745866955,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("B113", "B115", "B116", "B117", "B118", "B119", "B120", "B121", "B122", "B123", "B124", "B125")),
            Floor("2F", listOf("B201", "B201-8", "B211電腦教室", "B222", "B225", "B229")),
            Floor("3F", listOf("B323", "B337 ", "B338物理實驗室", "B339", "B342", "B344", "B345")),
            Floor("4F", listOf("B442", "B443 ", "B444", "B445")),
            Floor("5F", listOf("B501", "B504 ", "B506", "B508"))
        )
    ),
    Building(
        name = "工學院",
        latitude = 22.758378275850113,
        longitude = 120.3380465682743,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("F106", "F107", "F108", "F109", "F124", "F126", "F128", "F129", "F131", "F146", "F147", "F151")),
            Floor("2F", listOf("F208", "F209", "F223", "F224", "F225")),
            Floor("4F", listOf("F410", "F452 ", "F455", "F456")),
            Floor("5F", listOf("F553", "F554"))
        )
    ),
    Building(
        name = "教師研究大樓",
        latitude = 22.757841040842898,
        longitude = 120.33893969278456,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "跨領域實作工廠",
        latitude = 22.756954524048076,
        longitude = 120.33984482203078,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("S109")),
            Floor("2F", listOf("S202", "S204", "S210", "S211"))
        )
    ),
    Building(
        name = "智慧製造實作工廠",
        latitude = 22.756676108314508,
        longitude = 120.34002358961506,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("2F", listOf("T212", "T210"))
        )
    ),
    Building(
        name = "實作工廠",
        latitude = 22.757395439159467,
        longitude = 120.34038336557204,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("V101", "V106", "V107"))
        )
    ),
    Building(
        name = "職務宿舍",
        latitude = 22.758546559558063,
        longitude = 120.34080222175933,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapBrown
    ),
    Building(
        name = "第二停車場",
        latitude = 22.75875900588937,
        longitude = 120.33926928614007,
        description = "",
        icon = Icons.Filled.LocalParking,
        color = MapBlue
    ),
    Building(
        name = "第一停車場",
        latitude = 22.758538388533587,
        longitude = 120.33682810820846,
        description = "",
        icon = Icons.Filled.LocalParking,
        color = MapBlue
    ),
    Building(
        name = "東區校門口",
        latitude = 22.758537410117043,
        longitude = 120.33626519809775,
        description = "大學路校門口",
        icon = Icons.Filled.DoorFront,
        color = MapTeal
    ),
    Building(
        name = "機車棚",
        latitude = 22.758327050136636,
        longitude = 120.33554600964341,
        description = "",
        icon = Icons.Filled.TwoWheeler,
        color = MapPink
    ),
    Building(
        name = "第四停車場",
        latitude = 22.75769045685481,
        longitude = 120.33418598506351,
        description = "",
        icon = Icons.Filled.LocalParking,
        color = MapBlue
    ),
    Building(
        name = "第六停車場",
        latitude = 22.75617836224247,
        longitude = 120.33416308362759,
        description = "",
        icon = Icons.Filled.LocalParking,
        color = MapBlue
    ),
    Building(
        name = "行政大樓",
        latitude = 22.75679330288692,
        longitude = 120.33803382884696,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("國際事務處")),
            Floor("1F", listOf("衛生保健組", "文書組", "總收發室")),
            Floor("2F", listOf("學生事務處")),
            Floor("3F", listOf("總務處")),
            Floor("4F", listOf("教務處", "會議室(A440)")),
            Floor("5F", listOf("秘書室", "會議室(A523 A527)")),
            Floor("6F", listOf("校長室", "副校長室", "秘書室", "會議室(A607)"))
        )
    )
)

/** 由 iOS 的 QijinMapView.swift 自動轉換 */
val QijinBuildings: List<Building> = listOf(
    Building(
        name = "校門口",
        latitude = 22.608711,
        longitude = 120.271963,
        description = "旗津校區正門口",
        icon = Icons.Filled.DoorFront,
        color = MapTeal
    ),
    Building(
        name = "渤海樓(學生宿舍)",
        latitude = 22.60917106633049,
        longitude = 120.27217320069572,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapBlue
    ),
    Building(
        name = "船舶機械實習工廠",
        latitude = 22.60935033047928,
        longitude = 120.2725106020911,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "商船船員訓練中心",
        latitude = 22.609291221928917,
        longitude = 120.27281192550456,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "教學碼頭",
        latitude = 22.609328,
        longitude = 120.273096,
        description = "",
        icon = Icons.Filled.Sailing,
        color = MapGray
    ),
    Building(
        name = "行政大樓(鄭和樓)",
        latitude = 22.608989140381926,
        longitude = 120.27310696848046,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "教學實習綜合大樓",
        latitude = 22.608630,
        longitude = 120.272723,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("1101船舶機械工廠", "1104輪機實驗室", "1105船電實習教室", "1110主輔機工廠", "旗U101", "旗U102", "旗U103", "旗U104", "船藝教室(1F)")),
            Floor("2F", listOf("1204海資多功能教室", "1205海事風電研討室", "旗U201", "旗U202", "旗U203", "旗U204")),
            Floor("3F", listOf("旗306海資電腦教室", "旗307簡報室", "旗U301", "旗U302", "旗U303", "旗U304", "旗U305階梯教室", "旗U308輪機系會議室", "旗U310-1博班研討室", "旗U313海事機電研究", "旗U314學士後專班1", "旗U314學士後專班2")),
            Floor("4F", listOf("1405自動控制實驗室", "1406電子實習實驗室", "1409航海氣象教室", "1412海圖教室", "旗402航技專題", "旗U403教室", "旗U404教室", "旗U405教室", "旗U406教室", "旗U407教室", "旗U408階梯教室", "羅經教室(4F)")),
            Floor("5F", listOf("旗U509教室", "旗U510教室", "旗U511教室", "旗U512教室", "旗U513簡報室", "旗U523多功能教室", "航技系專業教室(5F)")),
            Floor("6F", listOf("1608海事資訊資料庫實驗室", "1612貨油實驗室", "1616雷達模擬教室", "英語園區自學教室(1609)", "旗U603教室", "旗U604教室", "旗U605教室", "旗津1610電腦B教室", "旗津1611語言教室", "航技系視訊教室(6F)"))
        )
    ),
    Building(
        name = "海事工程實習大樓",
        latitude = 22.608394,
        longitude = 120.273705,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("海事實習教室101", "海事實習教室102"))
        )
    ),
    Building(
        name = "立體機車停車場",
        latitude = 22.607845233283363,
        longitude = 120.27271326031023,
        description = "",
        icon = Icons.Filled.TwoWheeler,
        color = MapPink
    ),
    Building(
        name = "普化、物實驗室",
        latitude = 22.607684,
        longitude = 120.272957,
        description = "",
        icon = Icons.Filled.Science,
        color = MapGreen,
        classrooms = listOf(
            Floor("1F", listOf("101旗津物理實驗室")),
            Floor("2F", listOf("201旗津化學實驗室"))
        )
    ),
    Building(
        name = "艇庫",
        latitude = 22.60873910306279,
        longitude = 120.27345355000048,
        description = "",
        icon = Icons.Filled.Sailing,
        color = MapGray
    ),
    Building(
        name = "籃球場",
        latitude = 22.608056939812027,
        longitude = 120.2732389732737,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "體育館",
        latitude = 22.611750266626665,
        longitude = 120.27059959680412,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "教職員宿舍",
        latitude = 22.61223382641597,
        longitude = 120.26999933244961,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapBlue
    ),
    Building(
        name = "教職員宿舍",
        latitude = 22.612554,
        longitude = 120.270239,
        description = "",
        icon = Icons.Filled.Bed,
        color = MapBlue
    ),
    Building(
        name = "圖書館",
        latitude = 22.611959720651075,
        longitude = 120.27061195990814,
        description = "",
        icon = Icons.Filled.Book,
        color = MapBlue
    ),
    Building(
        name = "游泳池",
        latitude = 22.61144411502106,
        longitude = 120.2709784325244,
        description = "",
        icon = Icons.Filled.Pool,
        color = MapBlue
    ),
    Building(
        name = "滅火場",
        latitude = 22.611628,
        longitude = 120.270985,
        description = "",
        icon = Icons.Filled.FireExtinguisher,
        color = MapPink
    )
)

/** 由 iOS 的 NanziMapView.swift 自動轉換 */
val NanziBuildings: List<Building> = listOf(
    Building(
        name = "校門口",
        latitude = 22.72476877387302,
        longitude = 120.31467204016155,
        description = "楠梓校區正門口",
        icon = Icons.Filled.DoorFront,
        color = MapTeal
    ),
    Building(
        name = "立誠樓",
        latitude = 22.72542799905657,
        longitude = 120.31411812795353,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("4102階梯教室", "4103教室", "4106教室", "4107教室", "4108教室", "4109教室")),
            Floor("2F", listOf("4201教室", "4204教室", "4205書報討論室", "4206電腦實驗室")),
            Floor("3F", listOf("4306訊號與影像處理實驗室")),
            Floor("4F", listOf("4401A基礎通訊實驗室", "4405低軌衛星實驗室")),
            Floor("5F", listOf("4501DSP實驗室", "4502印刷電路製作室", "4507電子實驗室"))
        )
    ),
    Building(
        name = "致遠樓",
        latitude = 22.72582768176294,
        longitude = 120.31384121108714,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("3102多功能休閒遊憩專業教室", "3103觀光導覽專業教室", "3105教室", "3106海洋資源管理專業教室")),
            Floor("2F", listOf("3201專業教室", "3202教室", "3203教室", "3204教室", "3205教室", "3206教室")),
            Floor("4F", listOf("3401商資科技教學中心", "3409教室")),
            Floor("5F", listOf("3501供應鏈發展教室", "3508供應鏈管理教室"))
        )
    ),
    Building(
        name = "寰宇樓",
        latitude = 22.726257069135166,
        longitude = 120.31411606955142,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("2102英語教學教室", "2104語言學習園區", "2105郵輪與遊艇研討會", "2108會議展覽研究室")),
            Floor("2F", listOf("2202教室", "2203實驗室", "2204階梯教室", "2206教室", "2209專題教室", "2210專題教室")),
            Floor("3F", listOf("2304資訊科技研討室", "2310教室")),
            Floor("4F", listOf("2401教室", "2402教室", "2403商資應用教學中心", "2407管理科學研習教室", "2408商務知能創新中心")),
            Floor("5F", listOf("2501流通教學平台", "2505綠色運籌中心", "2506電腦輔助電話調查", "2507教室", "2508教室"))
        )
    ),
    Building(
        name = "行政大樓",
        latitude = 22.726485677888277,
        longitude = 120.31468842851496,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("聯合辦公室", "聯合服務中心", "開標會議室", "校安中心", "國際事務處", "楠梓校區總務行政組", "總機房", "綜合業務處處長室", "文件收發室")),
            Floor("2F", listOf("職業安全衛生組", "環境保護組", "環境安全衛生中心主任室", "聯合辦公室")),
            Floor("3F", listOf("校長室", "副校長室", "秘書室", "經營管理處", "經營管理處會議室", "經營管理處處長室", "會客室")),
            Floor("4F", listOf("第一會議室", "第二會議室", "海洋科技發展處", "海洋產業創新研究中心", "國際海事公約研究中心", "共同教育學院教師研究室")),
            Floor("5F", listOf("共同教育學院", "外語教育中心", "基礎教育中心", "博雅教育中心", "藝術文化中心", "體育室", "共同教育學院教師研究室")),
            Floor("6F", listOf("諮商輔導組", "原住民族學生資源中心", "資源教室")),
            Floor("7F", listOf("第三會議室", "第四會議室")),
            Floor("8F", listOf("第五會議室"))
        )
    ),
    Building(
        name = "弘德樓",
        latitude = 22.726257069135166,
        longitude = 120.31519179778198,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("5102教室", "5103教室", "5109教室", "5110階梯教室")),
            Floor("2F", listOf("5202教室", "5203教室", "5208海工所實驗室")),
            Floor("3F", listOf("5304教室", "5305造船專業教室", "5306機電整合實驗室")),
            Floor("4F", listOf("5402流體力學實驗室", "5403船用流力實驗室", "5407多用途教室")),
            Floor("5F", listOf("5502振動實驗室", "5505物理實驗室", "5506物理實驗室"))
        )
    ),
    Building(
        name = "大仁樓",
        latitude = 22.725858770497993,
        longitude = 120.3154703117419,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("6104通用技能實驗室", "6105儀控整合實驗室", "6106/6107製程設備實驗室", "6108真空系統實驗室")),
            Floor("2F", listOf("6203元件測量實驗室", "6204元件模組實驗室")),
            Floor("3F", listOf("6304半導體會議室")),
            Floor("4F", listOf("6404電子電路實驗室")),
            Floor("5F", listOf("6503客製化實驗室", "6504階梯教室", "6505微控器實驗室", "6501SOC實驗室"))
        )
    ),
    Building(
        name = "大信樓",
        latitude = 22.725434545822086,
        longitude = 120.3151585805207,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("7103教室", "7104教室", "7107海事學院會議室", "7109教室", "7110階梯教室")),
            Floor("2F", listOf("7202教室", "7203教室", "7204教室", "7205微型教室", "7208教室")),
            Floor("3F", listOf("7301短期學者訪問室", "7304海環大會議室")),
            Floor("4F", listOf("7402會議室")),
            Floor("5F", listOf("7501環境共同實驗室", "7508研究生研習室"))
        )
    ),
    Building(
        name = "停車場",
        latitude = 22.72705837650434,
        longitude = 120.31510747708352,
        description = "",
        icon = Icons.Filled.LocalParking,
        color = MapBlue
    ),
    Building(
        name = "司令台",
        latitude = 22.727145577315564,
        longitude = 120.31538854621739,
        description = "",
        icon = Icons.Filled.Campaign,
        color = MapGray
    ),
    Building(
        name = "體育館",
        latitude = 22.727411892973223,
        longitude = 120.31679644712814,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "運動場",
        latitude = 22.727138506987647,
        longitude = 120.31592768795886,
        description = "",
        icon = Icons.Filled.FitnessCenter,
        color = MapOrange
    ),
    Building(
        name = "科技潛水暨水域運動教學發展中心",
        latitude = 22.72712672309625,
        longitude = 120.31738158198347,
        description = "",
        icon = Icons.Filled.Pool,
        color = MapBlue,
        classrooms = listOf(
            Floor("1~3F", listOf("潛水池"))
        )
    ),
    Building(
        name = "水產食品實習大樓附設實習商店",
        latitude = 22.72672371341869,
        longitude = 120.31709284729953,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("2F", listOf("冷凍工程教室", "中餐烘焙教室")),
            Floor("3F", listOf("水食工廠專業教室", "水食工廠階梯教室"))
        )
    ),
    Building(
        name = "籃球場",
        latitude = 22.726165154177654,
        longitude = 120.31732792333743,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "排球場",
        latitude = 22.72631834575532,
        longitude = 120.31683477476618,
        description = "",
        icon = Icons.Filled.SportsVolleyball,
        color = MapYellow
    ),
    Building(
        name = "網球場",
        latitude = 22.726037887198057,
        longitude = 120.3168245540704,
        description = "",
        icon = Icons.Filled.SportsTennis,
        color = MapGreen
    ),
    Building(
        name = "沙灘排球場",
        latitude = 22.726035530401024,
        longitude = 120.31647705041395,
        description = "",
        icon = Icons.Filled.SportsVolleyball,
        color = MapYellow
    ),
    Building(
        name = "造船實習工廠",
        latitude = 22.725656085539782,
        longitude = 120.31723082672919,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1~3F", listOf("造船實習工廠-1", "造船實習工廠-2", "造船實習工廠-3", "造船實習工廠-4", "造船實習工廠-5", "造船實習工廠-6", "造船實習工廠-7", "造船實習工廠-8", "造船實習工廠-9", "造船實習工廠-10"))
        )
    ),
    Building(
        name = "海天樓",
        latitude = 22.7253119917825,
        longitude = 120.3166661332875,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("10101教室", "10102教室", "10103教室", "10104教室", "10105教室", "10106教室", "10107教室")),
            Floor("2F", listOf("10201教室", "10202教室", "10203教室", "10204教室", "10205教室", "10206教室", "10207教室", "10208教室")),
            Floor("3F", listOf("A301教室", "A302教室", "A305教室", "10307階梯教室")),
            Floor("4F", listOf("10401多媒體教室", "10403藝文教室", "10405階梯教室"))
        )
    ),
    Building(
        name = "藝文中心",
        latitude = 22.725509963638288,
        longitude = 120.31332396559789,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapPurple
    ),
    Building(
        name = "厚生樓",
        latitude = 22.725654460240044,
        longitude = 120.31293933312811,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("A11108-2水食系多功能教室", "A11114專業教室E", "A11127漁撈作業實習", "A11128專業教室F", "121-1教室")),
            Floor("2F", listOf("A11203漁管系會議室", "A11212-5水食系會議室", "212-3專業教室A", "212-4專業教室B", "A11213-5水產生物實驗室", "213-6漁航科技實驗室", "240-1專業教室C", "240-2專業教室D")),
            Floor("3F", listOf("A11301院級生物實驗室", "A11309食品檢驗分析實驗", "A11313院級生化分析實驗", "A11314院級微生物實驗室", "A11315-2水圈會議室", "317-1研究生教室B", "317-2研究生教室A")),
            Floor("4F", listOf("A11415海生系實驗室")),
            Floor("5F", listOf("A11515院級研討室A", "A11516院級研討室B", "A11527海生系研究生討論")),
            Floor("7F", listOf("A11714-2水食所專題", "A11719階梯教室", "A11726-專業教室"))
        )
    ),
    Building(
        name = "樂群樓",
        latitude = 22.725842482304497,
        longitude = 120.31230901403248,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("1F", listOf("8107教室", "8108教室", "8109階梯教室")),
            Floor("2F", listOf("8201化學教學教室", "8202教室", "8204教室", "8206教室")),
            Floor("3F", listOf("8302系大會議室", "8305系小會議室")),
            Floor("4F", listOf("8404魚類學實驗室")),
            Floor("5F", listOf("8504水質分析實驗室", "8508水產動物病理實驗室", "8501研究室", "8506研究室"))
        )
    ),
    Building(
        name = "英才樓",
        latitude = 22.72609977523596,
        longitude = 120.31238411588089,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray,
        classrooms = listOf(
            Floor("B1F", listOf("9001創客教室", "9003會議室", "9005海洋牧場")),
            Floor("1F", listOf("9102教室", "9103教室")),
            Floor("3F", listOf("化學實驗室"))
        )
    ),
    Building(
        name = "停車場",
        latitude = 22.72642881262024,
        longitude = 120.31160627528313,
        description = "",
        icon = Icons.Filled.LocalParking,
        color = MapBlue
    ),
    Building(
        name = "學生活動中心",
        latitude = 22.726589620072655,
        longitude = 120.31262551471153,
        description = "",
        icon = Icons.Filled.SportsBasketball,
        color = MapOrange
    ),
    Building(
        name = "圖書資訊視聽館",
        latitude = 22.726658890915772,
        longitude = 120.31346236387954,
        description = "",
        icon = Icons.Filled.Book,
        color = MapBlue,
        classrooms = listOf(
            Floor("5F", listOf("11504電腦教室(A)", "11505電腦教室(B)", "11511CAD教室"))
        )
    ),
    Building(
        name = "慧海樓",
        latitude = 22.727119046345106,
        longitude = 120.31338457984445,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "東海樓",
        latitude = 22.727213056402693,
        longitude = 120.31297151967817,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "黃海樓",
        latitude = 22.727517351675626,
        longitude = 120.31258796380048,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "南海樓",
        latitude = 22.72719573876862,
        longitude = 120.31221781897615,
        description = "",
        icon = Icons.Filled.Apartment,
        color = MapGray
    ),
    Building(
        name = "7-ELEVEN",
        latitude = 22.726965661378863,
        longitude = 120.31288568898525,
        description = "",
        icon = Icons.Filled.Storefront,
        color = MapGreen
    )
)