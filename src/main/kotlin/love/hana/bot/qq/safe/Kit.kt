package love.hana.bot.qq.safe

import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.green.model.v20180509.ImageSyncScanRequest
import com.aliyuncs.http.FormatType
import com.aliyuncs.profile.DefaultProfile
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import org.bson.Document
import org.json.JSONArray
import org.json.JSONObject

object Kit {
    private val DB_CONNECT = MongoClients.create("mongodb://root:55107888AAA@127.0.0.1")

    val CONFIG: MongoCollection<Document> = DB_CONNECT.getDatabase("qq-bot").getCollection("safe")

    private val KMS = DB_CONNECT.getDatabase("website").getCollection("kms")

    private val KEY = KMS.find(Filters.eq("platform", "alibaba.bot")).first()

    private val TOKEN_ID = KEY?.getString("token_id")

    private val TOKEN_SECRET = KEY?.getString("token_secret")

    private val AUTH = DefaultProfile.getProfile("cn-shenzhen", TOKEN_ID, TOKEN_SECRET)

    private val CLIENT = DefaultAcsClient(AUTH)

    suspend fun check(images: ArrayList<Image>): HashMap<Image, Boolean> {
        DefaultProfile.addEndpoint("cn-shenzhen", "Green", "green.cn-shenzhen.aliyuncs.com")
        val request = ImageSyncScanRequest()
        val payload = JSONObject()
        payload.put("scenes", JSONArray().put("porn"))
        payload.put("tasks", JSONArray())
        for (i in images) {
            val task = JSONObject()
            task.put("dataId", i.imageId)
            task.put("url", i.queryUrl())
            payload.getJSONArray("tasks").put(task)
        }
        request.setHttpContent(payload.toString().toByteArray(), "UTF-8", FormatType.JSON)
        val response = CLIENT.doAction(request)
        if (response != null && response.isSuccess) {
            val result = JSONObject(String(response.httpContent))
            if (result.getInt("code") == 200) {
                val data = HashMap<Image, Boolean>()
                for (j in 0 until result.getJSONArray("data").length()) {
                    val task = result.getJSONArray("data").getJSONObject(j)
                    if (task.getInt("code") == 200) {
                        data[Image.fromId(task.getString("dataId"))] = task.getJSONArray("results").getJSONObject(0).getString("suggestion").equals("block")
                    }
                    else {
                        data[Image.fromId(task.getString("dataId"))] = false
                    }
                }
                return data
            }
            else {
                throw Exception("Oh! We can't finish the requests. Requests ID: ${result.getString("requestId")}")
            }
        }
        else {
            throw Exception("Oh! We can't finish the requests. Maybe it was caused by Internet connection!")
        }
    }
}