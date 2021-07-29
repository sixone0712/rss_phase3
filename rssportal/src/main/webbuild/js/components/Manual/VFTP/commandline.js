import React, {useEffect, useState} from "react";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {Button, Card, CardBody, CardHeader} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronCircleDown, faExclamationCircle, faPlay} from "@fortawesome/free-solid-svg-icons";
import ConfirmModal from "../../Common/ConfirmModal";
import AlertModal from "../../Common/AlertModal";
import * as Define from "../../../define";
import {ScaleLoader} from "react-spinners";
import * as API from "../../../api";
import services from "../../../services";
import useStatusHook from "../../../hooks/useStatusHook";

const RSSCommandLine = ({ type, string, modalMsgList, checkRequest, requestData, saveData }) => {
    const [modalType, setModalType] = useState("ready");
    const [prevModal, setPrevModal] = useState("ready");
    const [modalMsg, setModalMsg] = useState("");
    const [startProcess, stopProcess, statusDetail, statusInfo, setPostData]
        = useStatusHook(type === "compat/optional" ? Define.VFTP_HOOK_COMPAT_DOWNLOAD : Define.VFTP_HOOK_SSS_SEARCH);

    const setModalOpen = (type) => {
        setPrevModal(modalType);
        setModalType(type);
        switch (type) {
            case "confirm": setModalMsg(modalMsgList.confirm); break;
            case "cancel": setModalMsg(modalMsgList.cancel);break;
            case "ready": setModalMsg("");break;
            case "process": setModalMsg(modalMsgList.process);break;
            case "complete": setModalMsg(modalMsgList.complete);break;
        }
    };

    const closeModal = () => {
        setModalOpen("ready") ;
    };

    const cancelModal = async (yesno) => {
        if (yesno === "yes") {
            stopProcess();
            closeModal();
        } else {
            if(type === "compat/optional") {
                if(!statusInfo.success) setModalOpen(prevModal);
                else setModalOpen("complete");
            } else {
                if(!statusInfo.success) setModalOpen(prevModal);
                else closeModal();
            }
        }
    };

    const completeModal = () => {
        if(type === "compat/optional") {
            saveData(statusDetail.downloadUrl);
            closeModal();
        }
    };

    const confirmLeftBtnFunc = () => {
        const result = checkRequest();

        if(result !== Define.RSS_SUCCESS) {
            setModalOpen("alert");
            setModalMsg(API.getErrorMsg(result));
        } else {
            setModalOpen("process");
            setPostData(requestData());
            startProcess();
        }
    };

    useEffect(() => {
        if(statusInfo.success) {
            console.log("type", type);
            if(type === "sss/optional") {
                services.axiosAPI.requestGet(statusDetail.resultUrl).then(res => {
                    saveData(res);
                    setModalOpen("ready");
                });
            } else {
                if(modalType !== "cancel") setModalOpen("complete");
            }
        } else if(statusInfo.failure) {
            setModalMsg(API.getErrorMsg(Define.SEARCH_FAIL_SERVER_ERROR));
            setModalOpen("alert");
        }
    }, [statusInfo.success, statusInfo.failure])

    return (
        <>
            <Card className="ribbon-wrapper command-line-card">
                <div className="ribbon ribbon-clip ribbon-command-line">
                    Current Command
                </div>
                <CardHeader>
                    <p>The following command will be executed.</p>
                    <div className="execute-btn-area">
                        <Button color="info" outline onClick={()=> setModalOpen("confirm")}>
                            <FontAwesomeIcon icon={faPlay} />
                        </Button>
                    </div>
                </CardHeader>
                <CardBody>
                    <div className="command-line">
                        Rapid Collector
                        { type === "compat/optional" ? "# get " : "# cd " }
                        { string }
                    </div>
                </CardBody>
            </Card>
            <ConfirmModal
                isOpen={modalType === "confirm"}
                icon={faExclamationCircle}
                message={modalMsg}
                leftBtn={"Execute"}
                rightBtn={"Cancel"}
                style={"administrator"}
                actionBg={closeModal}
                actionLeft={confirmLeftBtnFunc}
                actionRight={closeModal}
            />
            <ConfirmModal isOpen={modalType === "cancel"}
                          icon={faExclamationCircle}
                          message={modalMsg}
                          style={"administrator"}
                          leftBtn={"Yes"}
                          rightBtn={"No"}
                          actionBg={null}
                          actionLeft={()=>cancelModal("yes")}
                          actionRight={()=>cancelModal("no")}
            />
            <ConfirmModal isOpen={modalType === "complete"}
                          icon={faChevronCircleDown}
                          message={modalMsg}
                          leftBtn={"Save"}
                          rightBtn={"Cancel"}
                          style={"administrator"}
                          actionBg={null}
                          actionLeft={completeModal}
                          actionRight={()=> cancelModal("yes")}
            />
            <AlertModal isOpen={modalType === "alert"} icon={faExclamationCircle} message={modalMsg} style={"administrator"} closer={closeModal} />
            {modalType === "process" ? (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                >
                    <div className="Custom-modal-overlay" />
                    <div className="Custom-modal">
                        <div className="content-without-title">
                            <div className="spinner-area">
                                <ScaleLoader
                                    loading={true}
                                    height={50}
                                    width={16}
                                    radius={8}
                                    margin={4}
                                    css={{ height: "60px", display: "flex", alignItems: "center" }}
                                />
                            </div>
                            <p style={{ marginBottom: 0 }}>{modalMsg}</p>
                            { type === "compat/optional"
                                ?
                                    <div>
                                        <p>File: {statusDetail.downloadedFiles}/{statusDetail.totalFiles} <br/>
                                           DownloadedSize: ({(statusDetail.downloadSize === 0)?"0 Bytes" : API.bytesToSize(statusDetail.downloadSize)})</p>
                                    </div>
                                :
                                    <div>
                                        <p>Searched Files : {statusDetail.searchedCnt}</p>
                                    </div>
                            }
                        </div>
                        <div className="button-wrap">
                            <button className="administrator alert-type" onClick={()=> setModalOpen("cancel")}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </ReactTransitionGroup>
            ) : (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                />
            )}
        </>
    );
}

export default RSSCommandLine;
