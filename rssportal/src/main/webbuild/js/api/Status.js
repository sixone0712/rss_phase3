import services from "../services";
import * as Define from "../define";

export const requestId = async (url, data) => {
    try {
        const res = await services.axiosAPI.requestPost(url, data);
        return res.data;
    } catch (error) {
        console.error(error);
        console.error(error.message);
       throw error(`Post '${url}' is Failed`);
    }
}

export const requestStatus = async ({ url, isCancelRef, errorFunc, doneFunc, inProcessFunc, cancelFunc }) => {
    const getStatus = () => {
        return services.axiosAPI.requestGet(url);
    }
    const iterator = geneStatus(getStatus);
    const next = async ({ value, done }) => {
        console.log('[requestStatus][next]done', done);
        console.log('[requestStatus][next]value', value);
        console.log('[requestStatus][next]isCancelRef.current', isCancelRef.current);
        if (isCancelRef.current) {
            cancelFunc();
            return;
        }

        if (done) {
            if (value.status === Define.OK) {
                if (value.data.status === "error") {
                    errorFunc();
                    return;
                }
                doneFunc(value.data);
            } else {
                errorFunc();
            }
        } else {
            if(value.status === 200) {
                inProcessFunc(value.data);
                next(await iterator.next());
            } else {
                errorFunc();
            }
        }
    }
    next(await iterator.next());
}

export const geneStatus = async function* (func) {
    while (true) {
        let response;
        try {
            response = await func();
        } catch (e) {
            response = e;
        }
        yield response;

        if (response.status === 200) {
            const {status} = response.data;
            if (status === "done" || status === "error") return response;
        } else {
            return response;
        }

        function delay() {
            return new Promise((resolve) => setTimeout(() => {
                resolve();
            }, 500));
        }

        await delay();
        yield response;
    }
}