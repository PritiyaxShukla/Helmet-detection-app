

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadService {
    @Multipart
    @POST("/predict")  // Replace with the correct endpoint https://bike-helmet-detection-4vt9.onrender.com/
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<ResponseBody>
}

