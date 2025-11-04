package ordinary.rahmatbakery.api

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json


object SupabaseManager {

    private const val SUPABASE_URL = "https://fsiuefdwcbdhunfhbiwl.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZzaXVlZmR3Y2JkaHVuZmhiaXdsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk5MzM0NDMsImV4cCI6MjA3NTUwOTQ0M30.pSATGpW89fntkKRuF-qvC7wiO1oZsTruDd-1wMjOdIU"

    val client : SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth){
            alwaysAutoRefresh = true
            autoSaveToStorage = true

        }
        install(Postgrest)
        defaultSerializer = KotlinXSerializer(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
        install(Realtime)
        //install other modules
    }
}