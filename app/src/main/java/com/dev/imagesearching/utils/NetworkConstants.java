package com.dev.imagesearching.utils;

/**
 * (c) All rights reserved.
 *
 * Utility class to keep various required network constants.
 */
public interface NetworkConstants {

    // Base endpoint url to be used to hit the remote API's.
    String BASE_ENDPOINT_URL = "https://api.imgur.com";

    String IMAGE_BASE_URL = "https://i.imgur.com/";

    // Currently Imgur API version is 3.
    String IMGUR_API_VERSION = "3";

    // URI for image list API call.
    String IMAGE_PATH_URI = "/gallery/search/{pageNo}";

    // Key name for using the authorization header for images search network request.
    String AUTHORIZATION_HEADER_KEY = "Authorization";

    // Authorization header value for images search network request.
    // TODO: For the time, it can be hardcoded, but later on this can be dynamically updated.
    String AUTHORIZATION_HEADER_VALUE = "Client-ID 137cda6b5008a7c";


    int CODE_DEFAULT = -111;
    int CODE_UNKNOWN = -1;
    int CODE_INTERNET_NOT_AVAILABLE = 1001;
    int CODE_400 = 400;
    int CODE_401 = 401;
    int CODE_404 = 404;
    int CODE_500 = 500;
    int CODE_503 = 503;

    String UNKNOWN_FAILURE = "Unknown failure, Unable to process.";
    String CODE_INTERNET_NOT_AVAILABLE_DETAILS = "Internet is not available, Unable to process.";
    String CODE_400_DETAIL = "Bad request, Unable to process.";
    String CODE_401_DETAIL = "Unauthorized, Unable to process.";
    String CODE_404_DETAIL = "Not found, Unable to process.";
    String CODE_500_DETAIL = "Internal server error, Unable to process.";
    String CODE_503_DETAIL = "Forbidden, Unable to process.";
}
