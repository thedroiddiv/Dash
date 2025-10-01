package com.thedroiddiv.dash.data.repository

import com.thedroiddiv.dash.domain.models.Episode
import com.thedroiddiv.dash.domain.models.Video
import com.thedroiddiv.dash.domain.repository.VideoRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class StarryRepository(
    val client: HttpClient
) : VideoRepository {

    @OptIn(InternalAPI::class)
    override suspend fun fetchVideoDetails(
        videoId: String,
        userToken: String
    ): Result<Video> {
        try {
            val body = "{\"deeplink_url\":\"/in/$videoId\",\"app_launch_count\":10}"
            println(body)
            val response = client.post("https://www.hotstar.com/api/internal/bff/v2/start") {
                header("Content-Type", "application/json")
                header("x-hs-usertoken", userToken)
                header("X-HS-Platform", "web")
                header("X-Country-Code", "in")
                this.body = body
            }
            val responseBody = response.bodyAsText()
            println(responseBody)
            val responseJson = Json.parseToJsonElement(responseBody)

            // If non 200 response, return immediately
            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception(responseBody))
            }

            // If 200 response, try to parse
            val responseJsonObject = responseJson.jsonObject

            // Got the video details
            val isResolved = responseJsonObject["success"]?.jsonObject
                ?.get("is_deeplink_resolved")?.jsonPrimitive?.boolean
            if (isResolved == true) {
                val video = withContext(Dispatchers.Default) {
                    val isShow = videoId.contains("shows")
                    parseSuccessResponse(responseJsonObject, isShow)
                }
                return Result.success(video)
            }

            // Token expired, redirected to choose profile screen
            val isProfileScreen = responseJsonObject["success"]?.jsonObject
                ?.get("is_pre_launch")?.jsonPrimitive?.boolean
            if (isProfileScreen == true) {
                return Result.failure(Exception("Token expired, get fresh token using current token"))
            }

            // Couldn't parse body
            return Result.failure(Exception("Couldn't read body!"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun parseSuccessResponse(response: JsonObject, isShow: Boolean): Video {
        val successObject = response["success"]?.jsonObject
            ?: throw Exception("Missing success object")

        val pageObject = successObject["page"]?.jsonObject
            ?: throw Exception("Missing page object")

        val spacesObject = pageObject["spaces"]?.jsonObject
            ?: throw Exception("Missing spaces object")

        val heroSpace = spacesObject["hero"]?.jsonObject
            ?: throw Exception("Missing hero space")

        val heroWidgetWrappers = heroSpace["widget_wrappers"]?.jsonArray
            ?: throw Exception("Missing hero widget_wrappers")

        val heroWidget = heroWidgetWrappers.firstOrNull()?.jsonObject
            ?.get("widget")?.jsonObject
            ?: throw Exception("Missing hero widget")

        val heroData = heroWidget["data"]?.jsonObject
            ?: throw Exception("Missing widget data")

        val contentInfo = heroData["content_info"]?.jsonObject
            ?: throw Exception("Missing content_info")

        // Extract basic information
        val title = contentInfo["title"]?.jsonPrimitive?.content
            ?: throw Exception("Missing title")

        val description = contentInfo["description"]?.jsonPrimitive?.content
            ?: throw Exception("Missing description")

        val heroImg = heroData["hero_img"]?.jsonObject
            ?.get("src")?.jsonPrimitive?.content
            ?: throw Exception("Missing hero image")

        val thumbnail = "https://img1.hotstarext.com/image/upload/f_auto/$heroImg"

        // Determine content type from the instrumentation context
        val contextValue = heroWidget["widget_commons"]?.jsonObject
            ?.get("instrumentation")?.jsonObject
            ?.get("instrumentation_context_v2")?.jsonObject
            ?.get("value")?.jsonPrimitive?.content
            ?: ""

        return if (isShow) {
            val episodes = parseEpisodes(spacesObject)
            Video.Show(
                episodes = episodes,
                thumbnail = thumbnail,
                title = title,
                description = description
            )
        } else {
            Video.Movie(
                thumbnail = thumbnail,
                title = title,
                description = description
            )
        }
    }

    private fun parseEpisodes(spacesObject: JsonObject): List<Episode> {
        val episodes = mutableListOf<Episode>()

        val traySpace = spacesObject["tray"]?.jsonObject ?: return episodes
        val widgetWrappers = traySpace["widget_wrappers"]?.jsonArray ?: return episodes

        // Find the CategoryTrayWidget (Episodes section)
        val episodesWidget = widgetWrappers.firstOrNull { wrapper ->
            wrapper.jsonObject["template"]?.jsonPrimitive?.content == "CategoryTrayWidget"
        }?.jsonObject?.get("widget")?.jsonObject

        val trayItems = episodesWidget?.get("data")?.jsonObject
            ?.get("tray_items")?.jsonObject
            ?.get("data")?.jsonObject
            ?.get("items")?.jsonArray
            ?: return episodes

        trayItems.forEach { item ->
            val playableContent = item.jsonObject["playable_content"]?.jsonObject
                ?.get("data")?.jsonObject
                ?: return@forEach

            val episodeTitle = playableContent["title"]?.jsonPrimitive?.content ?: ""
            val episodeDescription = playableContent["description"]?.jsonPrimitive?.content ?: ""

            val posterSrc = playableContent["poster"]?.jsonObject
                ?.get("src")?.jsonPrimitive?.content ?: ""
            val episodeThumbnail = "https://img1.hotstarext.com/image/upload/f_auto/$posterSrc"

            // Extract episode number from tags (e.g., "S1 E1")
            val tags = playableContent["tags"]?.jsonArray
            val episodeTag = tags?.firstOrNull()?.jsonObject
                ?.get("value")?.jsonPrimitive?.content ?: ""

            // Parse episode number from format "S1 E1" -> 1
            val episodeNumber = episodeTag.substringAfterLast("E", "0")
                .trim()
                .toIntOrNull() ?: 0

            episodes.add(
                Episode(
                    number = episodeNumber,
                    thumbnail = episodeThumbnail,
                    title = episodeTitle,
                    description = episodeDescription
                )
            )
        }

        return episodes
    }
}