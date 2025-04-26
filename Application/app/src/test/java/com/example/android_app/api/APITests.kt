package com.example.android_app.api

import com.example.android_app.model.ComplexSearchResponse
import com.example.android_app.model.Recipe
import com.example.android_app.model.RecipeDetails
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiTests {

    private lateinit var server: MockWebServer
    private lateinit var api: SpoonacularApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))                       // point at mock server
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(SpoonacularApi::class.java)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }
    // Simulates a complexSearch response and verifies that searchRecipesComplex() returns the expected list of Recipe objects.
    @Test
    fun `searchRecipesComplex returns parsed list`() = runBlocking {

        val json = """
      {
        "results": [
          {
            "id": 123,
            "title": "Test Recipe",
            "image": "http://example.com/img.jpg",
            "imageType": "jpg"
          }
        ]
      }
    """.trimIndent()
        server.enqueue(MockResponse().setBody(json).setResponseCode(200))


        val resp: ComplexSearchResponse = api.searchRecipesComplex(
            includeIngredients = "apple,banana",
            diet               = "vegetarian",
            intolerances       = "gluten",
            number             = 1,
            sort               = "min-missing-ingredients",
            sortDirection      = "asc",
            apiKey             = "dummy"
        )


        assertNotNull(resp)
        assertEquals(1, resp.results.size)
        val r: Recipe = resp.results[0]
        assertEquals(123,      r.id)
        assertEquals("Test Recipe", r.title)
        assertEquals("http://example.com/img.jpg", r.image)
    }
    // Simulates a recipe information response and verifies that getRecipeDetails() correctly parses it into a RecipeDetails instance.
    @Test
    fun `getRecipeDetails parses detail correctly`() = runBlocking {
        val detailJson = """
      {
        "id": 555,
        "title": "Detail Recipe",
        "extendedIngredients": [
          {"original": "1 cup sugar", "name": "sugar"}
        ],
        "instructions": "Mix ingredients.",
        "nutrition": {
          "nutrients": [
            {"name": "Calories", "amount": 200.0, "unit": "kcal"}
          ]
        }
      }
    """.trimIndent()
        server.enqueue(MockResponse().setBody(detailJson).setResponseCode(200))

        val detail: RecipeDetails = api.getRecipeDetails(
            recipeId         = 555,
            includeNutrition = true,
            apiKey           = "dummy"
        )

        assertEquals(555, detail.id)
        assertEquals("Detail Recipe", detail.title)

        // Ingredients
        assertNotNull(detail.extendedIngredients)
        assertEquals(1, detail.extendedIngredients!!.size)
        with(detail.extendedIngredients!![0]) {
            assertEquals("1 cup sugar", original)
            assertEquals("sugar", name)
        }

        // Instructions
        assertEquals("Mix ingredients.", detail.instructions)

        // Nutrition
        val nuts = detail.nutrition!!.nutrients
        assertEquals(1, nuts.size)
        assertEquals("Calories", nuts[0].name)
        assertEquals(200.0, nuts[0].amount, 0.0)
        assertEquals("kcal", nuts[0].unit)
    }
}