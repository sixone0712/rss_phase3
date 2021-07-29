import React, {Component} from "react";
import {Button, ButtonToggle, Card, CardBody, Input, Table} from "reactstrap";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBan, faChevronCircleDown, faDownload, faExclamationCircle, faSearch} from "@fortawesome/free-solid-svg-icons";
import {faFileAlt, faFolder} from "@fortawesome/free-regular-svg-icons";
import ScaleLoader from "react-spinners/ScaleLoader";
import {Select} from "antd";
import CheckBox from "../../Common/CheckBox";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as searchListActions from "../../../modules/searchList";
import * as dlHistoryAction from "../../../modules/dlHistory";
import * as API from "../../../api";
import * as Define from '../../../define';
import services from "../../../services";
import {filePaginate, RenderPagination} from "../../Common/CommonFunction";
import ConfirmModal from "../../Common/ConfirmModal";
import AlertModal from "../../Common/AlertModal";
import _ from "lodash";
import {withStatusHook} from "../../../hooks/useStatusHook";

const { Option } = Select;

class FileList extends Component {
  constructor(props) {
    super(props);
    this.state = {
      pageSize: 10,
      currentPage: 1,
      isError: Define.RSS_SUCCESS,
      isDownloadOpen: false,
      isProcessOpen: false,
      isCancelOpen: false,
      isCompleteOpen: false,
      isAlertOpen: false,
      modalMessage: "",
      searchCompletedDate: props.requestCompletedDate,
      onSearch: false,
    };
  }

  static getDerivedStateFromProps(nextProps, prevState) {
    if(nextProps.requestCompletedDate !== prevState.searchCompletedDate) {
      console.log("[getDerivedStateFromProps] init filelist state");
      return {
        pageSize: 10,
        currentPage: 1,
        isError: Define.RSS_SUCCESS,
        isDownloadOpen: false,
        isProcessOpen: false,
        isCancelOpen: false,
        isCompleteOpen: false,
        isAlertOpen: false,
        modalMessage: "",
        searchCompletedDate: nextProps.requestCompletedDate,
        onSearch: false,
      }
    }
    return prevState;
  }

  setErrorMsg = (errCode) => {
    const  msg = API.getErrorMsg(errCode);
    if (msg.toString().length > 0) {
      this.setState({
        modalMessage: msg
      });
      return true;
    }
    return false;
  };

  openDownloadModal = () => {
    if(this.props.downloadCnt <= 0) {
      this.setErrorStatus(Define.FILE_FAIL_NO_ITEM);
      this.setErrorMsg(Define.FILE_FAIL_NO_ITEM);
      this.openAlertModal();
    } else {
      this.setErrorStatus(Define.RSS_SUCCESS);
      this.setState({
        isDownloadOpen: true,
        modalMessage: "Do you want to download the selected file?"
      });
    }
  };

  closeDownloadModal = () => {
    this.setState({
      isDownloadOpen: false,
      modalMessage: ""
    });
  };

  createDlRequestData = () => {
    const sortedList = this.props.sortedList.toJS();
    return {
      lists: sortedList.reduce((acc, cur, idx) => {
              if (cur.checked) acc.push({
                fabName: cur.structId,
                machineName: cur.targetName,
                categoryCode: cur.logId,
                categoryName: cur.logName,
                fileName: cur.fileName,
                fileSize: cur.fileSize,
                fileDate: cur.fileDate,
                file: cur.file,
              });
              return acc;
          }, [])
    };
  }

  requestDownload = async () => {
    const { setPostData, startProcess} = this.props;
    this.setErrorStatus(Define.RSS_SUCCESS);
    setPostData(this.createDlRequestData());
    startProcess();
  };

  openProcessModal = async () => {
    this.closeDownloadModal();
    setTimeout(() => {
      this.setState({
        isProcessOpen: true
      });
    }, 100);
    this.requestDownload().then(r => r);
  };

  closeProcessModal = () => {
    this.setState({
      isProcessOpen: false
    });
  };

  openCancelModal = () => {
    this.setState({
      isCancelOpen: true,
      modalMessage: "Are you sure you want to cancel the download?"
    });
  };

  closeCancelModal = async (isCancel) => {
    if (isCancel) {
      const { stopProcess } = this.props;
      stopProcess();
      this.closeAllModal();
    } else {
      const { status } = this.props.statusDetail;
      // If the download has already been completed, open openCompleteModal.
      if(status === "done") {
        this.setState({
          isProcessOpen: false,
          isCancelOpen: false,
          modalMessage: ""
        });
        this.openCompleteModal();
      } else {
        this.setState({
          isCancelOpen: false,
          modalMessage: ""
        });
      }
    }
  };

  openCompleteModal = () => {
      this.setState({
        isCompleteOpen: true,
        modalMessage: "Download Complete!"
      });
  };

  closeCompleteModal = async (isSave) => {
    this.closeAllModal();
    if(isSave) {
      const { downloadUrl } = this.props.statusDetail;
      try {
        const res = await services.axiosAPI.downloadFile(downloadUrl);
      } catch (err) {
        console.log(err);
        console.log(err.message);
      }
    }
  };

  openAlertModal = () => {
    this.setState({
      isAlertOpen: true
    });
  };

  closeAlertModal = () => {
    this.setState({
      isAlertOpen: false,
      modalMessage: ""
    });
  };

  closeAllModal = () => {
    this.setState({
      ...this.state,
      isDownloadOpen: false,
      isProcessOpen: false,
      isCancelOpen: false,
      isCompleteOpen: false,
      isAlertOpen: false,
      modalMessage: ""
    });
  }

  setErrorStatus = (error) => {
    this.setState({
      isError: error
    })
  };

  handlePageChange = page => {
    this.setState({
      currentPage: page
    });
  };

  onChangeRowsPerPage = (value) => {
    const { pageSize, currentPage } = this.state;
    const startIndex = (currentPage - 1) * pageSize === 0 ? 1 : (currentPage - 1) * pageSize + 1;

    this.setState({
      pageSize: parseInt(value),
      currentPage: Math.ceil(startIndex / parseInt(value))
    });
  };

  checkFileItem = (e) => {
    if(e !== null && e !== undefined) {
      const idx = e.target.id.split('_{#div#}_')[1];
      if (idx !== null && idx !== undefined) {
        const { searchListActions } = this.props;
        searchListActions.searchCheckResponseList(idx);
      }
      e.stopPropagation();
    }
  };

  handleTrClick = e => {
    if(e !== null && e !== undefined) {
      const id = e.target.parentElement.getAttribute("cbinfo");
      if (id !== null && id !== undefined) {
        const { searchListActions } = this.props;
        searchListActions.searchCheckResponseList(id);
      }
      e.stopPropagation();
    }
  };

  checkAllFileItem = (checked) => {
    const { searchListActions } = this.props;
    searchListActions.searchCheckALLResponseList(checked);
  };

  handleThClick = key => {
    const { sortKey, sortDirection, searchListActions } = this.props;
    let changeDirection = "asc";
    if (sortKey === key && sortDirection === "asc") {
      changeDirection = "desc";
    }
    searchListActions.searchSetSortKeyDirection({ sortKey: key, sortDirection: changeDirection });
  };

  sortIconRender = name => {
    const { sortKey, sortDirection } = this.props;
    const style = "sort-icon";
    return sortKey === name ? style + " sort-active " + sortDirection : style;
  };

  componentDidUpdate(prevProps, prevState, snapshot) {
    const { success: prevSuccess, failure: prevFailure }  = prevProps.statusInfo;
    const { success: curSuccess, failure: curFailure }  = this.props.statusInfo;

    if(prevSuccess === false && curSuccess === true) {
      if(this.state.isCancelOpen !== true) this.openCompleteModal();
    } else if(prevFailure === false && curFailure === true) {
      this.closeAllModal();
      this.setErrorMsg(Define.FILE_FAIL_SERVER_ERROR)
      this.openAlertModal();
    }
  }

  render() {
    const { responseListCnt, requestCompletedDate, sortFolderFile, query } = this.props;
    const sortedList = this.props.sortedList.toJS();
    const {
      pageSize,
      currentPage,
      isDownloadOpen,
      isProcessOpen,
      isCancelOpen,
      isCompleteOpen,
      isAlertOpen,
      modalMessage,
    } = this.state;

    if (responseListCnt === 0) {
      return (
          <Card className="ribbon-wrapper filelist-card">
            <CardBody className=".filelist-card-body">
              <div className="ribbon ribbon-clip ribbon-info">File</div>
              <div className="filelist-no-search">
                <p>
                  <FontAwesomeIcon icon={faExclamationCircle} size="7x" />
                </p>
                <p>{requestCompletedDate === "" ? "Search has not started yet." : "Logs not found."}</p>
              </div>
            </CardBody>
          </Card>
      );
    } else {
      const { downloadCnt, responseIsFolder } = this.props;
      const selectedFilesCnt = sortedList.filter(list => list.checked && list.fileType === "F").length;
      const selectedFoldersCnt = sortedList.filter(list => list.checked && list.fileType !== "F").length;
      const itemsChecked = sortedList.length === downloadCnt;
      const files = filePaginate(sortedList, currentPage, pageSize);
      const { totalFiles, downloadedFiles ,downloadSize, totalSize, status } = this.props.statusDetail;

      return (
        <>
          <ConfirmModal isOpen={isDownloadOpen}
                        icon={faDownload}
                        message={modalMessage}
                        style={"secondary"}
                        leftBtn={"Yes"}
                        rightBtn={"No"}
                        actionBg={this.closeDownloadModal}
                        actionLeft={this.openProcessModal}
                        actionRight={this.closeDownloadModal}
          />
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
                    <p className="no-margin-no-padding">
                      { API.convertDownloadMsg(status) }
                    </p>
                    {(totalFiles > 0)  &&
                    <p className="no-margin-no-padding">
                      File: ({downloadedFiles}/{totalFiles}) <br/>
                      {
                        responseIsFolder
                          ? `DownloadedSize: (${downloadSize ? API.bytesToSize(downloadSize) : "0 Byte"})`
                          : `DownloadedSize: (${downloadSize ? API.bytesToSize(downloadSize) : "0 Byte"}/${API.bytesToSize(totalSize)})`
                      }
                    </p>
                    }
                  </div>
                  <div className="button-wrap">
                    <button
                        className="secondary alert-type"
                        onClick={this.openCancelModal}
                    >
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
                        icon={faBan}
                        message={modalMessage}
                        style={"secondary"}
                        leftBtn={"Yes"}
                        rightBtn={"No"}
                        actionBg={null}
                        actionLeft={() => this.closeCancelModal(true)}
                        actionRight={() => this.closeCancelModal(false)}
          />
          <ConfirmModal isOpen={isCompleteOpen}
                        icon={faChevronCircleDown}
                        message={modalMessage}
                        leftBtn={"Save"}
                        rightBtn={"Cancel"}
                        style={"secondary"}
                        actionBg={null}
                        actionLeft={() => this.closeCompleteModal(true)}
                        actionRight={() => this.closeCompleteModal(false)}
          />
          <AlertModal isOpen={isAlertOpen} icon={faExclamationCircle} message={modalMessage} style={"secondary"} closer={this.closeAlertModal} />
          <Card className="ribbon-wrapper filelist-card">
            <CardBody className="filelist-card-body">
              <div className="ribbon ribbon-clip ribbon-info">File</div>
              <Table>
                <thead>
                  <tr>
                    <th>
                      <div>
                        <ButtonToggle
                          outline
                          size="sm"
                          color="info"
                          className={
                            "filelist-btn filelist-btn-toggle" +
                            (itemsChecked ? " active" : "")
                          }
                          onClick={()=> this.checkAllFileItem(!itemsChecked)}
                        >
                          All
                        </ButtonToggle>
                      </div>
                    </th>
                    <th onClick={() => this.handleThClick("targetName")}>
                      <span className="sortLabel-root">
                        Machine
                        <span className={this.sortIconRender("targetName")}>➜</span>
                      </span>
                    </th>
                    <th onClick={() => this.handleThClick("logIdName")}>
                      <span className="sortLabel-root">
                        Category
                        <span className={this.sortIconRender("logIdName")}>➜</span>
                      </span>
                    </th>
                    <th onClick={() => this.handleThClick("fileName")}>
                      <span className="sortLabel-root">
                        File Name
                        <span className={this.sortIconRender("fileName")}>➜</span>
                      </span>
                    </th>
                    <th onClick={() => this.handleThClick("fileDate")}>
                      <span className="sortLabel-root">
                        Date
                        <span className={this.sortIconRender("fileDate")}>➜</span>
                      </span>
                    </th>
                    <th onClick={() => this.handleThClick("fileSize")}>
                      <span className="sortLabel-root">
                        Size
                        <span className={this.sortIconRender("fileSize")}>➜</span>
                      </span>
                    </th>
                  </tr>
                </thead>
                 <tbody>
                {files.map((file, key) => {
                  const convFileDate = API.convertDateFormat(file.fileDate);
                  const convFileName = [];

                  if(file.fileType === "F") {
                    if (file.fileName.indexOf("/") !== -1) {
                      const fileNameSplit = file.fileName.split("/");
                      for (let i = 0; i < fileNameSplit.length; i++) {
                        if (i === fileNameSplit.length - 1) {
                          convFileName.push(<><FontAwesomeIcon icon={faFileAlt}/>{" " + fileNameSplit[i]}</>);
                        } else {
                          convFileName.push(<>{fileNameSplit[i] + " / "}</>);
                        }
                      }
                    } else {
                      convFileName.push(<><FontAwesomeIcon icon={faFileAlt}/>{" " + file.fileName}</>);
                    }
                  } else {
                    convFileName.push(<><FontAwesomeIcon icon={faFolder}/>{" " + file.fileName}</>);
                  }

                  return (
                      <tr
                          key={key}
                          onClick={(e) => this.handleTrClick(e)}
                          cbinfo={file.keyIndex}
                      >
                        <td>
                          <div className="custom-control custom-checkbox">
                            <CheckBox
                                index={file.keyIndex}
                                name={file.fileName}
                                isChecked={file.checked}
                                labelClass={"filelist-label"}
                                handleCheckboxClick={this.checkFileItem}
                            />
                          </div>
                        </td>
                        <td>{file.targetName}</td>
                        <td>{file.logIdName}</td>
                        <td>{convFileName}</td>
                        <td>{convFileDate}</td>
                        <td>{file.fileType === "F" ? API.bytesToSize(file.fileSize) : "-"}</td>
                      </tr>
                  );
                })}
                </tbody>
              </Table>
            </CardBody>
            <RenderPagination
                pageSize={pageSize}
                itemsCount={sortedList.length}
                onPageChange={this.handlePageChange}
                currentPage={currentPage}
                className={"custom-pagination"}
            />
            <div className="filelist-info-area">
              {
                responseIsFolder
                    ?
                    <label style={{
                      whiteSpace: 'pre'
                    }}>
                      {`${this.props.downloadCnt} Selected  ( `}
                      <FontAwesomeIcon icon={faFolder} />
                      {` ${selectedFoldersCnt}   `}
                      <FontAwesomeIcon icon={faFileAlt} />
                      {` ${selectedFilesCnt} )`}
                    </label>
                    : <label>{this.props.downloadCnt} File Selected</label>
              }
            </div>

            <div className="filelist-item-area">
              <div className="func-section">
                <div>
                  <input
                      type="checkbox"
                      id="search-toggle"
                      checked={this.state.onSearch}
                      onChange={() => {
                        this.setState((prevState) => ({
                          onSearch: !prevState.onSearch
                        }))
                        const { searchListActions } = this.props;
                        searchListActions.searchSetSortQuery("");
                      }}
                  />
                  <label htmlFor="search-toggle" className="search-toggle">
                    <FontAwesomeIcon icon={faSearch} />
                  </label>
                </div>
                <div className={"input-section" + (this.state.onSearch ? " show" : "")}>
                  <Input
                      type="text"
                      tabIndex={-1}
                      placeholder="Enter the file name to search."
                      value={query}
                      onChange={(e) => {
                        const { searchListActions } = this.props;
                        searchListActions.searchSetSortQuery(e.target.value)
                        this.setState({
                          currentPage: 1
                        })
                      }}
                  />
                </div>
              </div>
              { responseIsFolder &&
                <>
                  <label>Sort:</label>
                  <Select
                    defaultValue={sortFolderFile}
                    onChange={(value) => {
                      const { searchListActions } = this.props;
                      searchListActions.searchSetSortFolderFile(value);
                      this.setState({
                        currentPage: 1
                      });
                    }}
                    className="filelist"
                  >
                    <Option value={Define.SORT_FOLDER_FILE.ALL}>
                      <FontAwesomeIcon icon={faFolder} />
                      <span style={{ marginLeft: "5px"}}/>
                      <FontAwesomeIcon icon={faFileAlt} />
                    </Option>
                    <Option value={Define.SORT_FOLDER_FILE.FOLDER}>
                      <FontAwesomeIcon icon={faFolder} />
                    </Option>
                    <Option value={Define.SORT_FOLDER_FILE.FILE}>
                      <FontAwesomeIcon icon={faFileAlt} />
                    </Option>
                  </Select>
                  </>
              }

              <label>Rows per page:</label>
              <Select
                  defaultValue={10}
                  value={pageSize}
                  onChange={this.onChangeRowsPerPage}
                  className="filelist"
              >
                <Option value={10}>10</Option>
                <Option value={30}>30</Option>
                <Option value={50}>50</Option>
                <Option value={100}>100</Option>
              </Select>
              <Button
                  outline
                  size="sm"
                  color="info"
                  className="filelist-btn"
                  onClick={this.openDownloadModal}
              >
                Download
              </Button>
            </div>
          </Card>
        </>
      );
    }
  }
}

export default connect(
    (state) => ({
      responseListCnt: state.searchList.get('responseListCnt'),
      downloadCnt: state.searchList.get('downloadCnt'),
      requestCompletedDate: state.searchList.get('requestCompletedDate'),
      responseIsFolder: state.searchList.get('responseIsFolder'),
      sortedList: state.searchList.get('sortedList'),
      query: state.searchList.get('query'),
      sortKey: state.searchList.get('sortKey'),
      sortDirection: state.searchList.get('sortDirection'),
      sortFolderFile: state.searchList.get('sortFolderFile'),
    }),
    (dispatch) => ({
      searchListActions: bindActionCreators(searchListActions, dispatch),
      dlHistoryAction: bindActionCreators(dlHistoryAction, dispatch)
    })
)(withStatusHook(FileList, Define.FTP_HOOK_DOWNLOAD));
