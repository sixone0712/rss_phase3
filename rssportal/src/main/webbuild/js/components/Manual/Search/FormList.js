import React, {Component} from "react";
import {Button, Card, CardBody, Col, FormGroup} from "reactstrap";
import ReactTransitionGroup from "react-addons-css-transition-group";
import ScaleLoader from "react-spinners/ScaleLoader";
import DatePicker from "./DatePicker";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../../modules/viewList";
import * as searchListActions from "../../../modules/searchList";
import * as API from "../../../api";
import * as Define from "../../../define";
import {faExclamationCircle} from "@fortawesome/free-solid-svg-icons";
import AlertModal from "../../Common/AlertModal";
import ConfirmModal from "../../Common/ConfirmModal";
import services from "../../../services";
import {withStatusHook} from "../../../hooks/useStatusHook";

export const modalType = {
    PROCESS: 1,
    CANCEL: 2,
    ALERT: 3,
    CANCEL_COMPLETE: 4,
    CAUTION_FOLDER: 5
};

class FormList extends Component{
    constructor(props) {
        super(props);
        this.state = {
            alertMsg: "",
            isProcessOpen: false,
            isCancelOpen: false,
            isAlertOpen: false,
            isCautionOpen: false,
        }
    }

    onSetErrorState = (errCode) => {
        let msg = "";

        switch (errCode) {
            case Define.SEARCH_FAIL_NO_MACHINE_AND_CATEGORY:
                msg = "Please choose a machine and category.";
                break;
            case Define.SEARCH_FAIL_DATE:
                msg = "Please set the start time before the end time.";
                break;
            case Define.SEARCH_FAIL_SERVER_ERROR:
                msg = "Network connection error.";
                break;
            default:
                break;
        }

        if (msg.toString().length > 0) {
            this.setState({
                alertMsg: msg
            });
            return true;
        }
        return false;
    };

    openModal = (type) => {
        switch(type) {
            case modalType.PROCESS:
                this.setState({
                    isProcessOpen: true
                });
                break;
                
            case modalType.CANCEL:
                this.setState({
                    isCancelOpen: true
                });
                break;
                
            case modalType.ALERT:
                this.setState({
                    isProcessOpen: false,
                    isCancelOpen: false,
                    isCautionOpen: false,
                    isAlertOpen: true
                });
                break;

            case modalType.CANCEL_COMPLETE:
                if(this.props.responseListCnt === 0) {
                    const { stopProcess } = this.props;
                    stopProcess();
                    this.setState({
                        isProcessOpen: false,
                        isCancelOpen: false,
                        isAlertOpen: false,
                        isCautionOpen: false,
                        alertMsg: "",
                    })
                }
                break;

            case modalType.CAUTION_FOLDER:
                this.setState({
                    isCautionOpen: true
                });
                break;

            default:
                console.log("[formlist.js] invalid modal type!!!!");
                break;
        }
    };

    closeModal = async (type) => {
        switch(type) {
            case modalType.PROCESS:
                try {
                    const { searchListActions } = this.props;
                    const { resultUrl } = this.props.statusDetail;
                    const res = await services.axiosAPI.requestGet(resultUrl);
                    searchListActions.searchSaveResponseList(res);
                } catch (e) {
                    console.error(e);
                    console.error(e.message);
                    this.onSetErrorState(Define.SEARCH_FAIL_SERVER_ERROR);
                    await this.openModal(modalType.ALERT);
                } finally {
                    this.setState({
                        isProcessOpen: false,
                        isCancelOpen: false     // cancel modal is going to close when search is complete in cancel modal.
                    });
                }
                break;

            case modalType.CANCEL:
                this.setState({
                    isCancelOpen: false
                });
                break;

            case modalType.ALERT:
                this.setState({
                    isAlertOpen: false,
                    alertMsg: ""
                });
                break;

            case modalType.CAUTION_FOLDER:
                this.setState({
                    isCautionOpen: false,
                    alertMsg: ""
                });
                break;

            default:
                this.setState({
                    isProcessOpen: false,
                    isCancelOpen: false,
                    isAlertOpen: false,
                    isCautionOpen: false,
                    alertMsg: ""
                })
                break;
        }
    }

    makeSearchRequestData = () => {
        const { requestList, searchDepth } = this.props;
        const requestListJS = requestList.toJS();

        return {
            fabNames: requestListJS.fabNames,
            machineNames: requestListJS.machineNames,
            categoryCodes: requestListJS.categoryCodes,
            categoryNames: requestListJS.categoryNames,
            startDate: requestListJS.startDate,
            endDate: requestListJS.endDate,
            folder: requestListJS.folder,
            depth: searchDepth,
        };
    }

    isValidRequest = async () => {
        const errCode = await API.setSearchList(this.props);
        if (this.onSetErrorState(errCode)) {
            this.openModal(modalType.ALERT);
            return false;
        }
        return true;
    }

    isValidFolderRequest = () => {
        const { logInfoListCheckCnt } = this.props;
        if(logInfoListCheckCnt !== 1) {
            this.setState({
                alertMsg: "Please choose only one category."
            })
            this.openModal(modalType.CAUTION_FOLDER);
            return false;
        }

        const { logInfoList } = this.props;
        const selectedCategory = logInfoList.find(item => item.get("checked") === true);
        const dest = selectedCategory.get("dest").toLowerCase();
        if(dest !== 'cons') {
            this.setState({
                alertMsg: "Please choose FTP(Cons) category."
            })
            this.openModal(modalType.CAUTION_FOLDER);
            return false;
        }

        return true;
    }

    requestSearch = async () => {
        const { searchListActions, setPostData, startProcess } = this.props;
        searchListActions.searchInitResponseList();
        this.openModal(modalType.PROCESS);
        setPostData(this.makeSearchRequestData());
        startProcess();
    }

    processSearch = async () => {
        const { isFolder } = this.props;
        if(await this.isValidRequest()) {
            if(isFolder) {
                if(!this.isValidFolderRequest()) {
                    return;
                }
            }
            this.requestSearch().then(r => r);
        }
    }

    onChangeFolder = () => {
        const { isFolder, searchListActions } = this.props;
        searchListActions.searchSetRequestFolder(!isFolder);
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        const { success: prevSuccess, failure: prevFailure }  = prevProps.statusInfo;
        const { success: curSuccess, failure: curFailure }  = this.props.statusInfo;

        if(prevSuccess === false && curSuccess === true) {
            this.closeModal(modalType.PROCESS).then(r => r);
        } else if(prevFailure === false && curFailure === true) {
            this.onSetErrorState(Define.SEARCH_FAIL_SERVER_ERROR);
            this.openModal(modalType.ALERT);
        }
    }

    render() {
        const { isProcessOpen, isCancelOpen, isAlertOpen, isCautionOpen, alertMsg } = this.state;
        const { isFolder } = this.props;
        const { statusDetail } = this.props;

        return (
            <Card className="ribbon-wrapper formlist-card">
                <CardBody className="custom-scrollbar manual-card-body">
                    <div className="ribbon ribbon-clip ribbon-success">Date</div>
                    <Col>
                        <FormGroup className="formlist-form-group">
                            { !isFolder && <DatePicker /> }
                        </FormGroup>
                    </Col>
                    <div className="card-btn-area">
                        <Button
                            outline size="sm"
                            color="info"
                            className={`formlist-btn ${isFolder ? " active" : ""}`}
                            onClick={this.onChangeFolder}
                        >
                            Folder
                        </Button>
                        <Button
                            outline size="sm"
                            color="info"
                            className="formlist-btn"
                            onClick={this.processSearch}
                        >
                            Search
                        </Button>
                        {isProcessOpen ? (
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
                                        <p>Searching...</p>
                                        <p>Searched Files : {statusDetail.searchedCnt}</p>
                                    </div>
                                    <div className="button-wrap">
                                        <button className="alert-type green" onClick={() => this.openModal(modalType.CANCEL)}>
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
                        <ConfirmModal isOpen={isCancelOpen}
                                      icon={faExclamationCircle}
                                      message={"Are you sure you want to cancel the search?"}
                                      style={"green"}
                                      leftBtn={"Yes"}
                                      rightBtn={"No"}
                                      actionBg={null}
                                      actionLeft={() => this.openModal(modalType.CANCEL_COMPLETE)}
                                      actionRight={() => this.closeModal(modalType.CANCEL)}
                        />
                        <AlertModal isOpen={isCautionOpen} icon={faExclamationCircle} message={alertMsg} style={"green"} closer={() => this.closeModal(modalType.CAUTION_FOLDER)} />
                        <AlertModal isOpen={isAlertOpen} icon={faExclamationCircle} message={alertMsg} style={"green"} closer={() => this.closeModal(modalType.ALERT)} />
                    </div>
                </CardBody>
            </Card>
        );
    }
}

export default connect(
    (state) => ({
      toolInfoListCheckCnt: state.viewList.get('toolInfoListCheckCnt'),
      logInfoListCheckCnt: state.viewList.get('logInfoListCheckCnt'),
      toolInfoList: state.viewList.get('toolInfoList'),
      logInfoList: state.viewList.get('logInfoList'),
      requestList: state.searchList.get('requestList'),
      requestCompleted: state.searchList.get('requestCompleted'),
      responseListCnt: state.searchList.get('responseListCnt'),
      startDate: state.searchList.get('startDate'),
      endDate: state.searchList.get('endDate'),
      isFolder: state.searchList.get('isFolder'),
      searchDepth: state.searchList.get('searchDepth'),
    }),
    (dispatch) => ({
      viewListActions: bindActionCreators(viewListActions, dispatch),
      searchListActions: bindActionCreators(searchListActions, dispatch)
    })
)(withStatusHook(FormList, Define.FTP_HOOK_SEARCH));