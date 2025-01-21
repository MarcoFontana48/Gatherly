package social.utils.http

import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import java.util.concurrent.CountDownLatch

object TestRequestUtils {
    fun sendPutRequest(
        send: JsonObject?,
        latch: CountDownLatch,
        endpoint: String,
        webClient: WebClient
    ): HttpResponse<String> {
        val responseLatch = CountDownLatch(1)
        lateinit var response: HttpResponse<String>
        webClient.put(endpoint)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(send) { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    throw ar.cause()
                }
                responseLatch.countDown()
            }
        responseLatch.await()
        return response
    }

    fun sendGetRequest(
        paramName: String,
        paramValue: String,
        latch: CountDownLatch,
        endpoint: String,
        webClient: WebClient
    ): HttpResponse<String> {
        val responseLatch = CountDownLatch(1)
        lateinit var response: HttpResponse<String>
        webClient.get(endpoint)
            .addQueryParam(paramName, paramValue)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .send { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    throw ar.cause()
                }
                responseLatch.countDown()
            }
        responseLatch.await()
        return response
    }

    fun sendGetRequest(
        paramName: String,
        paramValue: String,
        paramName2: String,
        paramValue2: String,
        latch: CountDownLatch,
        endpoint: String,
        webClient: WebClient
    ): HttpResponse<String> {
        val responseLatch = CountDownLatch(1)
        lateinit var response: HttpResponse<String>
        webClient.get(endpoint)
            .addQueryParam(paramName, paramValue)
            .addQueryParam(paramName2, paramValue2)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .send { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    throw ar.cause()
                }
                responseLatch.countDown()
            }
        responseLatch.await()
        return response
    }

    fun sendGetRequest(
        latch: CountDownLatch,
        endpoint: String,
        webClient: WebClient
    ): HttpResponse<String> {
        val responseLatch = CountDownLatch(1)
        lateinit var response: HttpResponse<String>
        webClient.get(endpoint)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .send { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    throw ar.cause()
                }
                responseLatch.countDown()
            }
        responseLatch.await()
        return response
    }

    fun sendPostRequest(
        send: JsonObject?,
        latch: CountDownLatch,
        endpoint: String,
        webClient: WebClient
    ): HttpResponse<String> {
        val responseLatch = CountDownLatch(1)
        lateinit var response: HttpResponse<String>
        webClient.post(endpoint)
            .putHeader("content-type", "application/json")
            .`as`(BodyCodec.string())
            .sendJsonObject(send) { ar ->
                latch.countDown()
                if (ar.succeeded()) {
                    response = ar.result()
                } else {
                    throw ar.cause()
                }
                responseLatch.countDown()
            }
        responseLatch.await()
        return response
    }
}
