package com.thedroiddiv.dash.ui.screen.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thedroiddiv.dash.domain.models.Episode
import com.thedroiddiv.dash.domain.models.Video
import com.thedroiddiv.dash.ui.screen.details.components.EpisodeCard
import com.thedroiddiv.dash.ui.screen.details.components.VideoHeroCard
import com.thedroiddiv.dash.ui.theme.DashTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailsScreen(
    modifier: Modifier = Modifier,
    uiState: UiState
) {
    var userToken by remember { mutableStateOf("") }
    var isTokenVisible by remember { mutableStateOf(false) }
    var videoId by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Video Details") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = userToken,
                            onValueChange = { userToken = it },
                            label = { Text("User Token") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (isTokenVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                    Icon(
                                        imageVector = if (isTokenVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (isTokenVisible)
                                            "Hide token"
                                        else
                                            "Show token"
                                    )
                                }
                            }
                        )

                        OutlinedTextField(
                            value = videoId,
                            onValueChange = { videoId = it },
                            label = { Text("Video ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        uiState.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = { /* Handle load */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Load Details")
                        }
                    }
                }
            }

            item {
                VideoHeroCard(video = uiState.video)
            }

            if (uiState.video is Video.Show) {
                item {
                    Text(
                        text = "Episodes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.video.episodes) { episode ->
                    EpisodeCard(episode = episode)
                }
            }
        }
    }
}


val sampleShowUiState = UiState(
    video = Video.Show(
        thumbnail = "https://picsum.photos/400/225?random=1",
        title = "Stranger Things",
        description = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
        episodes = listOf(
            Episode(
                number = 1,
                thumbnail = "https://picsum.photos/400/225?random=2",
                title = "Chapter One: The Vanishing of Will Byers",
                description = "On his way home from a friend's house, young Will sees something terrifying. Nearby, a sinister secret lurks in the depths of a government lab."
            ),
            Episode(
                number = 2,
                thumbnail = "https://picsum.photos/400/225?random=3",
                title = "Chapter Two: The Weirdo on Maple Street",
                description = "Lucas, Mike and Dustin try to talk to the girl they found in the woods. Hopper questions an anxious Joyce about an unsettling phone call."
            ),
            Episode(
                number = 3,
                thumbnail = "https://picsum.photos/400/225?random=4",
                title = "Chapter Three: Holly, Jolly",
                description = "An increasingly concerned Nancy looks for Barb and finds out what Jonathan's been up to. Joyce is convinced Will is trying to talk to her."
            ),
            Episode(
                number = 4,
                thumbnail = "https://picsum.photos/400/225?random=5",
                title = "Chapter Four: The Body",
                description = "Refusing to believe Will is dead, Joyce tries to connect with her son. The boys give Eleven a makeover. Nancy and Jonathan form an unlikely alliance."
            ),
            Episode(
                number = 5,
                thumbnail = "https://picsum.photos/400/225?random=6",
                title = "Chapter Five: The Flea and the Acrobat",
                description = "Hopper breaks into the lab while Nancy and Jonathan confront the force that took Will. The boys ask Mr. Clarke how to travel to another dimension."
            )
        )
    ),
    error = "You got this!"
)

val sampleMovieUiState = UiState(
    video = Video.Movie(
        thumbnail = "https://picsum.photos/400/225?random=7",
        title = "Inception",
        description = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O."
    )
)


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun VideoDetailsScreenPrev() {
    DashTheme {
        VideoDetailsScreen(uiState = sampleShowUiState)
    }
}