import React, {useEffect, useRef, useState} from 'react';
import services from "../services";
import * as API from "../api";
import * as Define from "../define";

const initialDownloadStatusDetail = {
    dlId: "",
    status: "",
    totalFiles: 0,
    downloadFiles: 0,
    downloadUrl: "",
    totalSize: 0,
    downloadSize: 0
}

const initialSearchStatusDetail = {
    searchId: "",
    searchedCnt: 0,
    status: "",
    resultUrl: "",
}

const initialStatusInfo = {
    start: false,
    success: false,
    failure: false
}

function getInitialStatusDetail(mode) {
    switch (mode) {
        case Define.FTP_HOOK_DOWNLOAD:
        case Define.VFTP_HOOK_COMPAT_DOWNLOAD:
        case Define.VFTP_HOOK_SSS_DOWNLOAD:
            return initialDownloadStatusDetail;
        case Define.FTP_HOOK_SEARCH:
        case Define.VFTP_HOOK_SSS_SEARCH:
            return initialSearchStatusDetail;
    }
}

function getRequestPostURL(mode) {
    switch (mode) {
        case Define.FTP_HOOK_SEARCH:
            return '/rss/api/ftp/search';
        case Define.FTP_HOOK_DOWNLOAD:
            return '/rss/api/ftp/download';
        case Define.VFTP_HOOK_COMPAT_DOWNLOAD:
            return '/rss/api/vftp/compat/download';
        case Define.VFTP_HOOK_SSS_DOWNLOAD:
            return '/rss/api/vftp/sss/download';
        case Define.VFTP_HOOK_SSS_SEARCH:
            return '/rss/api/vftp/sss/search';
    }
}

function getRequestId(mode, data) {
    switch (mode) {
        case Define.FTP_HOOK_DOWNLOAD:
        case Define.VFTP_HOOK_COMPAT_DOWNLOAD:
        case Define.VFTP_HOOK_SSS_DOWNLOAD:
            return data.downloadId;
        case Define.FTP_HOOK_SEARCH:
        case Define.VFTP_HOOK_SSS_SEARCH:
            return data.searchId;
    }
}

function getRequestGetURL(mode, requestId) {
    switch (mode) {
        case Define.FTP_HOOK_SEARCH:
            return `/rss/api/ftp/search/${requestId}`;
        case Define.FTP_HOOK_DOWNLOAD:
            return `/rss/api/ftp/download/${requestId}`;
        case Define.VFTP_HOOK_COMPAT_DOWNLOAD:
            return `/rss/api/vftp/compat/download/${requestId}`;
        case Define.VFTP_HOOK_SSS_DOWNLOAD:
            return `/rss/api/vftp/sss/download/${requestId}`;
        case Define.VFTP_HOOK_SSS_SEARCH:
            return `/rss/api/vftp/sss/search/${requestId}`;
    }
}

function getRequestDeleteURL(mode, requestId) {
    console.log("mode", mode);
    console.log("requestId", requestId);
    switch (mode) {
        case Define.FTP_HOOK_SEARCH:
            return `/rss/api/ftp/search/${requestId}`;
        case Define.FTP_HOOK_DOWNLOAD:
            return `/rss/api/ftp/download/${requestId}`;
        case Define.VFTP_HOOK_COMPAT_DOWNLOAD:
            return `/rss/api/vftp/compat/download/${requestId}`;
        case Define.VFTP_HOOK_SSS_DOWNLOAD:
            return `/rss/api/vftp/sss/download/${requestId}`;
        case Define.VFTP_HOOK_SSS_SEARCH:
            return `/rss/api/vftp/sss/search/${requestId}`;
    }
}

export default function useStatusHook(mode) {
    const [statusDetail, setStatusDetail] = useState({
        ...getInitialStatusDetail(mode),
    });
    const [statusInfo, setStatusInfo] = useState({
        ...initialStatusInfo
    });
    const [postData, setPostData] = useState(null);
    const requestIdRef = useRef('');
    const cancelRef = useRef(false);

    const errorFunc = () => {
        setStatusInfo((prevState) => ({
            ...prevState,
            failure: true
        }));
    }

    const doneFunc = (data) => {
        setStatusDetail(prevState => ({
            ...prevState,
            ...data
        }));
        setStatusInfo((prevState) => ({
            ...prevState,
            success: true
        }));
    }
    const inProcessFunc = (data) => {
        setStatusDetail({
            ...data
        });
    }

    const cancelFunc = () => {
        if(statusDetail.status !== "done") {
            services.axiosAPI.requestDelete(getRequestDeleteURL(mode, requestIdRef.current))
                .then(r => r)
                .catch(e => {
                    console.error(e);
                    console.error(e.message);
                });
        }
    }

    const requestPost = async () => {
        let resData;
        try {
            resData = await API.requestId(getRequestPostURL(mode), postData);
        } catch(error) {
            errorFunc();
            return;
        }
        const requestId = getRequestId(mode, resData);
        requestIdRef.current = requestId;
        console.log("[useStatusHook][requestPost]requestId", requestId);
        if (!requestId) {
            console.error("[useStatusHook][requestPost]requestId is null");
            errorFunc();
            return;
        }

        API.requestStatus({
            url: getRequestGetURL(mode, requestId),
            isCancelRef: cancelRef,
            errorFunc,
            doneFunc,
            inProcessFunc,
            cancelFunc
        }).then(r => r);
    }

    const startProcess = () => {
        cancelRef.current = false;
        requestIdRef.current = '';
        setStatusInfo({
            start: true,
            success: false,
            failure: false
        })
    }

    const stopProcess = () => {
        cancelRef.current = true;
    }

    useEffect(() => {
        if (statusInfo.start) {
            setStatusDetail({ ...getInitialStatusDetail(mode) });
            setStatusInfo({ ...initialStatusInfo });
            cancelRef.current = false;
            requestPost().then(r => r);
        }
    }, [statusInfo.start])

    return [startProcess, stopProcess, statusDetail, statusInfo, setPostData];
}

export function withStatusHook(Comp, mode){
    return (props) => {
        const [startProcess, stopProcess, statusDetail, statusInfo, setPostData] = useStatusHook(mode);
        return <Comp
                    {...props}
                    startProcess={startProcess}
                    stopProcess={stopProcess}
                    statusDetail={statusDetail}
                    statusInfo={statusInfo}
                    setPostData={setPostData}
        />;
    }
}