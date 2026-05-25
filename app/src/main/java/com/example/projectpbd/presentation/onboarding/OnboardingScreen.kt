package com.example.projectpbd.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projectpbd.presentation.components.MomentumDimensions as Dim
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            "Welcome to Momentum",
            "Your intelligent personal finance companion. Stay aware and stay intentional with your money.",
            Icons.Default.RocketLaunch
        ),
        OnboardingPage(
            "Track Income & Expenses",
            "Monitor every transaction with smart wallets. Categorize your spending automatically.",
            Icons.Default.AccountBalanceWallet
        ),
        OnboardingPage(
            "Goals & Savings Vaults",
            "Save intentionally and achieve goals faster with dedicated virtual vaults.",
            Icons.Default.Savings
        ),
        OnboardingPage(
            "Analytics & Insights",
            "Understand your financial behavior with intelligent insights and real-time trends.",
            Icons.Default.AutoGraph
        ),
        OnboardingPage(
            "Get Started",
            "Ready to take control of your financial future? Let's start managing your momentum today.",
            Icons.Default.CheckCircle
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { position ->
                OnboardingPageContent(pages[position])
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dim.SpacingXXLarge),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Page Indicator
                Row(horizontalArrangement = Arrangement.spacedBy(Dim.SpacingSmall)) {
                    repeat(pages.size) { index ->
                        val color = if (pagerState.currentPage == index) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.outlineVariant
                        
                        Surface(
                            modifier = Modifier.size(if (pagerState.currentPage == index) 12.dp else 8.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = color
                        ) {}
                    }
                }

                // Next/Finish Button
                Button(
                    onClick = {
                        if (pagerState.currentPage < pages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onFinished()
                        }
                    },
                    shape = RoundedCornerShape(Dim.RadiusMedium)
                ) {
                    Text(if (pagerState.currentPage == pages.size - 1) "Start Managing" else "Next")
                    if (pagerState.currentPage < pages.size - 1) {
                        Spacer(modifier = Modifier.width(Dim.SpacingSmall))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dim.SpacingXXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(Dim.SpacingXXLarge))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Dim.SpacingLarge))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Dim.SpacingLarge)
        )
    }
}
