import axios from 'axios';
import * as Define from "../define";
import { WritableStream } from 'web-streams-polyfill/ponyfill';

// Add a request interceptor
axios.interceptors.request.use(
    config => {
        const token = sessionStorage.getItem("accessToken");
        if(token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }
        config.headers['Content-Type'] = 'application/json';
        console.log("[axios.interceptors.request]config.headers", config.headers);
        return config;
    },
    error => {
        return Promise.reject(error);
    });

//Add a response interceptor
axios.interceptors.response.use(
    (response) => {
        console.log("[axios.interceptors.response.use] response", response);
        return response
    },
    function (error) {
        console.log("[axios.interceptors.response.use] error", error);
        console.log("[axios.interceptors.response.use] error.response", error.response);

        if(error.message === "Operation canceled by the user") {
            return Promise.reject(error);
        }

        // If an internal service error occurs, go to the network error page.
        if(error.response === undefined || error.response.status === Define.INTERNAL_SERVER_ERROR) {
              setTimeout(() => window.appHistory.replace(Define.PAGE_NEWORK_ERROR), 100)
              return Promise.reject(error);
            /*
            const serverError = {
                response: {
                    status: Define.INTERNAL_SERVER_ERROR,
                    data: {
                        error: {
                            reason: "internalError"
                        }
                    }
                }
            }
            return Promise.reject(serverError);
            */
        }

        const originalRequest = error.config;
        if (error.response.status === Define.UNAUTHORIZED && originalRequest.url === '/rss/api/auths/token') {
            console.log("[axios.interceptors.response.use] case 1");
            //router.push('/login');
            window.sessionStorage.clear();
            window.location.replace('/rss');
            return Promise.reject(error);
        }
        if (error.response.status === Define.UNAUTHORIZED && !originalRequest._retry) {
            console.log("[axios.interceptors.response.use] case 2");
            originalRequest._retry = true;
            const refreshToken = sessionStorage.getItem("refreshToken");
            return axios.post('/rss/api/auths/token', { "refreshToken": refreshToken })
                .then(res => {
                    //if (res.status === 201) {
                    if (res.status === Define.OK) {
                        sessionStorage.setItem("accessToken", res.data.accessToken);
                        axios.defaults.headers.common['Authorization'] = 'Bearer ' + sessionStorage.getItem("accessToken");
                        return axios(originalRequest);
                    }
                })
        }
        return Promise.reject(error);
    }
);

export const getCancelToken = axios.CancelToken;
export let getCancel;
export const postCancelToken = axios.CancelToken;
export let postCancel;
export const putCancelToken = axios.CancelToken;
export let putCancel;
export const patchCancelToken = axios.CancelToken;
export let patchCancel;
export const deleteCancelToken = axios.CancelToken;
export let deleteCancel;

export const requestGet = (url) => {
    return axios.get(url, {
        headers: {
            // Internet Explorer requests caching
            // If the cache header is not set, Internet Explorer 11 (and earlier) caches all resources by default,
            // and in case of successive get requests, the cached data is responded without sending a get request to the server.

            // This setting was not applied in IE11.
            //'Cache-Control': 'no-cache',

            // Add headers to all API requests on the client side
            'Pragma': 'no-cache'

            // References
            // https://cherniavskii.com/internet-explorer-requests-caching/
            // https://kdevkr.github.io/archives/2018/understanding-cache-control/
        },
        // References
        // https://yamoo9.github.io/axios/guide/cancellation.html
        cancelToken: new getCancelToken(function executor(c) {
            // The executor function takes the cancel function as a parameter.
            getCancel = c;
        })
    });
}

export const requestPost = (url, data) => {
    return axios.post(url, data, {
        headers: {
            'Content-Type': 'application/json',
        },
        // References
        // https://yamoo9.github.io/axios/guide/cancellation.html
        cancelToken: new postCancelToken(function executor(c) {
            // The executor function takes the cancel function as a parameter.
            postCancel = c;
        })
    });
}

export const requestPut = (url, data) => {
    return axios.put(url, data, {
        headers: {
            'Content-Type': 'application/json',
        },
        // References
        // https://yamoo9.github.io/axios/guide/cancellation.html
        cancelToken: new putCancelToken(function executor(c) {
            // The executor function takes the cancel function as a parameter.
            putCancel = c;
        })
    });
}

export const requestPatch = (url, data) => {
    return axios.patch(url, data, {
        headers: {
            'Content-Type': 'application/json',
        },
        // References
        // https://yamoo9.github.io/axios/guide/cancellation.html
        cancelToken: new patchCancelToken(function executor(c) {
            // The executor function takes the cancel function as a parameter.
            patchCancel = c;
        })
    });
}

export const requestDelete = async (url) => {
    return await axios.delete(url, {
        headers: {
            'Content-Type': 'application/json',
        },
        // References
        // https://yamoo9.github.io/axios/guide/cancellation.html
        cancelToken: new deleteCancelToken(function executor(c) {
            // The executor function takes the cancel function as a parameter.
            deleteCancel = c;
        })
    });
}

export const downloadFile = async (url) => {
    try {
        const checkUrl = url.replace('/storage/', '/validation/')
        const response = await requestGet(checkUrl);
    } catch (e) {
        console.error(e);
        if(e.response.status === Define.NOT_FOUND) {
            return {
                result: Define.COMMON_FAIL_NOT_FOUND,
            };
        } else {
            return {
                result: Define.COMMON_FAIL_SERVER_ERROR,
            };
        }
    }
    const token = sessionStorage.getItem("accessToken");
    const link = document.createElement('a');
    link.href = `${url}?accesstoken=${token}`;
    link.style.cssText = 'display:none';
    document.body.appendChild(link);
    link.click();
    link.remove();

    return {
        result: Define.RSS_SUCCESS,
    };
};

/*
import streamSaver from 'streamsaver';
streamSaver.mitm = `http://${window.location.host}/rss/mitm.html?version=2.0.0`
export const downloadFileOld = async (url) => {
    const token = sessionStorage.getItem("accessToken");
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
        });

        let contentDisposition = response.headers.get('Content-Disposition');
        let fileName = contentDisposition.substring(contentDisposition.lastIndexOf('=') + 1);

        console.log("response.statusCode", response.statusCode);
        if (!response.ok) {
            if(response.statusCode === 404) {
                return {
                    result: Define.COMMON_FAIL_NOT_FOUND,
                    fileName: "unknown",
                };
            } else {
                return {
                    result: Define.COMMON_FAIL_SERVER_ERROR,
                    fileName: "unknown",
                };
            }
        }

        // These code section is adapted from an example of the StreamSaver.js
        // https://jimmywarting.github.io/StreamSaver.js/examples/fetch.html

        // If the WritableStream is not available (Firefox, Safari), take it from the ponyfill
        if (!window.WritableStream) {
            streamSaver.WritableStream = WritableStream;
            window.WritableStream = WritableStream;
        }

        const fileStream = streamSaver.createWriteStream(fileName);
        const readableStream = response.body;

        // more optimized pipe version
        if (readableStream.pipeTo) {
            readableStream.pipeTo(fileStream);
            return {
                result: Define.RSS_SUCCESS,
                fileName: fileName,
            };
        }

        // Write (pipe) manually
        window.writer = fileStream.getWriter();
        const reader = response.body.getReader();
        const pump = () => reader.read()
            .then(res => res.done
                ? writer.close()
                : writer.write(res.value).then(pump));

        pump();
        return {
            result: Define.RSS_SUCCESS,
            fileName: fileName,
        };
    } catch(error) {
        console.log(error);
        return {
            result: Define.COMMON_FAIL_SERVER_ERROR,
            fileName: "unknown",
        };
    }
};
*/