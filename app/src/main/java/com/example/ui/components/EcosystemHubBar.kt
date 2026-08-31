package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkText
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryNavy
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SlateGray
import com.example.ui.viewmodels.EcosystemTab

data class EcosystemModuleItem(
    val tab: EcosystemTab,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null,
    val activeColor: Color,
    val iconBgColor: Color
)

val ecosystemModules = listOf(
    EcosystemModuleItem(
        tab = EcosystemTab.BOZOR,
        title = "BOZOR",
        subtitle = "Gagarin onlayn do‘konlar",
        icon = Icons.Filled.ShoppingBag,
        badge = "BOZOR",
        activeColor = Color(0xFF8B1E3F),
        iconBgColor = Color(0xFFFCE7ED)
    ),
    EcosystemModuleItem(
        tab = EcosystemTab.FOOD,
        title = "GAGARIN TAOMLAR",
        subtitle = "Restoran & Kafelar",
        icon = Icons.Filled.Restaurant,
        badge = "0 SO'M",
        activeColor = Color(0xFFEA580C),
        iconBgColor = Color(0xFFFFEDD5)
    ),
    EcosystemModuleItem(
        tab = EcosystemTab.ADS,
        title = "E'LONLAR",
        subtitle = "Bepul e'lon joylash",
        icon = Icons.Filled.Campaign,
        badge = "E'LON",
        activeColor = Color(0xFFDC2626),
        iconBgColor = Color(0xFFFEE2E2)
    ),
    EcosystemModuleItem(
        tab = EcosystemTab.SERVICES,
        title = "USTALAR",
        subtitle = "Usta va xizmatlar",
        icon = Icons.Filled.Handyman,
        badge = "XIZMAT",
        activeColor = Color(0xFF0891B2),
        iconBgColor = Color(0xFFCFFAFE)
    ),
    EcosystemModuleItem(
        tab = EcosystemTab.JOBS,
        title = "ISHLAR",
        subtitle = "Vakansiya & ish o‘rinlari",
        icon = Icons.Filled.Work,
        badge = "ISH",
        activeColor = Color(0xFF059669),
        iconBgColor = Color(0xFFD1FAE5)
    )
)

@Composable
fun EcosystemHubBar(
    currentTab: EcosystemTab,
    onTabSelected: (EcosystemTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CardSurface,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)) {
            // Live status banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gagarin Ekotizimi • Onlayn Xizmatlar",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF047857)
                    )
                }

                Text(
                    text = "Mirzacho‘l tumani",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryText
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Prominent Ecosystem Section Cards
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(ecosystemModules) { module ->
                    val isSelected = currentTab == module.tab
                    val cardBgColor by animateColorAsState(
                        if (isSelected) module.activeColor else Color.White,
                        label = "tab_card_bg"
                    )
                    val titleColor = if (isSelected) Color.White else DarkText
                    val subtitleColor = if (isSelected) Color.White.copy(alpha = 0.92f) else SecondaryText
                    val iconTint = if (isSelected) Color.White else module.activeColor
                    val iconContainerBg = if (isSelected) Color.White.copy(alpha = 0.22f) else module.iconBgColor
                    val borderColor = if (isSelected) module.activeColor else Color(0xFFE2E8F0)

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardBgColor,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = borderColor
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTabSelected(module.tab) }
                            .testTag("ecosystem_tab_${module.tab.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            // Large, Eye-Catching Colorful Icon Container
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(iconContainerBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = module.icon,
                                    contentDescription = module.title,
                                    tint = iconTint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Large & Bold Section Title with Subtitle
                            Column(
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = module.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.3.sp,
                                        color = titleColor
                                    )

                                    if (module.badge != null && !isSelected) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    module.activeColor.copy(alpha = 0.15f),
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = module.badge,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = module.activeColor
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = module.subtitle,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = subtitleColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
