import React, {useCallback, useEffect, useRef, useState} from "react";
import {Button, ButtonToggle, Card, CardBody, Input, Table} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faBan, faChevronCircleDown, faDownload, faExclamationCircle, faSearch} from "@fortawesome/free-solid-svg-icons";
import {faFileAlt} from "@fortawesome/free-regular-svg-icons";
import {Select} from "antd";
import ReactTransitionGroup from "react-addons-css-transition-group";
import ScaleLoader from "react-spinners/ScaleLoader";
import {filePaginate, RenderPagination} from "../../Common/CommonFunction";
import _ from "lodash";
import {useDispatch, useSelector} from "react-redux";
import {vftpSssSetIsNewResponseList} from "../../../modules/vftpSss";
import * as API from "../../../api";
import services from "../../../services"
import * as Define from "../../../define";
import produce from "immer";
import useStatusHook from "../../../hooks/useStatusHook";

const { Option } = Select;

/*
const propsCompare = (prevProps, nextProps) => {
    if (JSON.stringify(prevProps.fileList) === JSON.stringify(nextProps.fileList)) {
        return false;
    }

    return !(prevProps.checkedList.length === nextProps.checkedList.length &&
        prevProps.checkedList.sort().every((value, index) => {
            return value === nextProps.checkedList.sort()[index];
        }));
};
*/

const initialModalInfo = {
   isDownloadConfirm: false,
   isDownloadStart: false,
   isDownloadCancel: false,
   isDownloadComplete: false,
   isDownloadError: false,
   downloadErrorMsg: ''
}

export function usePrevious(value) {
    const ref = useRef();
    useEffect(() => {
        ref.current = value;
    });
    return ref.current;
}

const RSSvftpFilelist = () => {
    const responseList = useSelector(state => state.vftpSss.get('responseList'));
    const responseListCnt = useSelector(state => state.vftpSss.get('responseListCnt'));
    const requestCompletedDate = useSelector(state => state.vftpSss.get('requestCompletedDate'));
    const isNewResponseList = useSelector(state => state.vftpSss.get('isNewResponseList'));
    const dispatch = useDispatch();
    const [pageSize, setPageSize] = useState(10);
    const [currentPage, setCurrentPage] = useState(1);
    const [sortDirection, setSortDirection] = useState("");
    const [sortKey, setSortKey] = useState("");
    const [sortedList, setSortedList] = useState(responseList ? _.orderBy(responseList.toJS(), "", "") : []);
    const [modalInfo, setModalInfo] = useState(initialModalInfo)
    const [query, setQuery] = useState("");
    const [onSearch, setOnSearch] = useState(false);
    const [allItemChecked, setAllItemChecked] = useState(true);
    const [checkedCnt, setCheckedCnt] = useState(0);
    const [startProcess, stopProcess, statusDetail, statusInfo, setPostData] = useStatusHook(Define.VFTP_HOOK_SSS_DOWNLOAD);

    const setStateModalInfo = ({ isDownloadConfirm, isDownloadStart, isDownloadCancel, isDownloadComplete, isDownloadError, downloadErrorMsg}) => {
        setModalInfo(prevState => ({
            isDownloadConfirm: isDownloadConfirm !== undefined ? isDownloadConfirm : prevState.isDownloadConfirm,
            isDownloadStart: isDownloadStart !== undefined ? isDownloadStart : prevState.isDownloadStart,
            isDownloadCancel: isDownloadCancel !== undefined ? isDownloadCancel : prevState.isDownloadCancel,
            isDownloadComplete: isDownloadComplete !== undefined ? isDownloadComplete : prevState.isDownloadComplete,
            isDownloadError: isDownloadError !== undefined ? isDownloadError : prevState.isDownloadError,
            downloadErrorMsg: downloadErrorMsg !== undefined ? downloadErrorMsg : prevState.downloadErrorMsg,
        }));
    }

    useEffect(() => {
        if(responseList !== undefined) {
            if(isNewResponseList) {
                setPageSize(10);
                setCurrentPage(1);
                setSortDirection("");
                setSortKey("");
                setOnSearch(false);
                setQuery("");
                dispatch(vftpSssSetIsNewResponseList(false));
            }
            const responseListToJs = responseList.toJS();
            const newSortedList = sortedAllList(responseListToJs, query, sortKey, sortDirection)
            setSortedList(newSortedList);
            setCheckedCnt(newSortedList.length);
        }
    }, [responseList, isNewResponseList])

    // when there is a input on search
    useEffect(() => {
        if(responseList !== undefined) {
            const responseListToJs = responseList.toJS();
            if(query === "") {
                const newSortedList = sortedKeyDirectionList(responseListToJs, sortKey, sortDirection);
                setSortedList(newSortedList);
                setCheckedCnt(newSortedList.length);
            } else {
                const list = sortedAllList(responseListToJs, query, sortKey, sortDirection)
                const newSortedList = checkAllSortedList(list, true)
                setSortedList(newSortedList);
                setCheckedCnt(newSortedList.length);
            }
            setCurrentPage(1);
        }
    }, [query])

    // setting all item check button
    useEffect(() => {
        setAllItemChecked(sortedList.length <= checkedCnt);
    }, [checkedCnt])

    useEffect(() => {
        if(statusInfo.success) {
            if(!modalInfo.isDownloadCancel) openDownloadComplete();
        } else if(statusInfo.failure) {
            openDownloadStatusError();
        }
    }, [statusInfo.success, statusInfo.failure])


    const openDownloadConfirm = useCallback(() => {
        if (checkedCnt === 0) {
            setStateModalInfo({
                downloadErrorMsg: API.getErrorMsg(Define.FILE_FAIL_NO_ITEM),
                isDownloadError: true
            })
        } else {
            setStateModalInfo({
                isDownloadConfirm: true
            })
        }
    }, [checkedCnt]);

    const closeDownloadConfirm = useCallback(() => {
        setStateModalInfo({
            isDownloadConfirm: false,
        })
    }, []);

    const openDownloadStatusError = useCallback(() => {
        setTimeout(() => {
            setStateModalInfo({
                isDownloadConfirm: false,
                isDownloadStart: false,
                isDownloadCancel: false,
                isDownloadComplete: false,
                isDownloadError: true,
                downloadErrorMsg: API.getErrorMsg(Define.FILE_FAIL_SERVER_ERROR)
            })
        }, 400);
    }, []);

    const createDlRequestData = () => {
        return { lists: sortedList.filter(item => item.checked === true) };
    }

    const requestDownload = () => {
        setPostData(createDlRequestData());
        startProcess();
    };

    const openDownloadStart = () => {
        setStateModalInfo({
            isDownloadConfirm: false,
        });
        setTimeout(() =>
            setStateModalInfo({
                isDownloadStart: true
            }), 400);
        requestDownload();
    }

    const openDownloadCancel = useCallback(() => {
        setStateModalInfo({
            isDownloadStart: false,
        });
        setTimeout(() =>
            setStateModalInfo({
                isDownloadCancel: true,
            }), 400);
    }, []);

    const closeDownloadCancel = useCallback((type) => {
        setStateModalInfo({
            isDownloadCancel: false,
        });
        if (type !== "OK") {
           if (statusDetail.status === "done") {
               setTimeout(() =>
                   setStateModalInfo({
                       isDownloadComplete: true,
                   }), 400);
           } else {
               setTimeout(() =>
                   setStateModalInfo({
                       isDownloadStart: true,
                   }), 400);
           }
        } else {
            stopProcess();
            setStateModalInfo({
                ...initialModalInfo
            });
        }
    }, [statusDetail.status])

    const openDownloadComplete = useCallback(() => {
        setStateModalInfo({
            isDownloadStart: false,
        });
        setTimeout(() =>
            setStateModalInfo({
                isDownloadComplete: true,
            }), 400);
    }, []);

    // save file
    const closeDownloadComplete = useCallback(async (isSave) => {
        setStateModalInfo({
            isDownloadComplete: false,
        });
        if(isSave) {
            try {
                const res = await services.axiosAPI.downloadFile(statusDetail.downloadUrl);
            } catch (e) {
                console.error(e);
                console.error(e.message);
            }
        }
    }, [statusDetail.downloadUrl]);

    const closeDownloadError = useCallback(() => {
        setStateModalInfo({
            isDownloadError: false,
        });
    }, []);

    const handleCheckboxClick = e => {
         if(e !== null && e !== undefined) {
            const idx = e.target.id;
            if (idx !== null && idx !== undefined) {
                const { lists, checked } = checkSortedList(sortedList, idx);
                setSortedList(lists);
                setCheckedCnt(checked ? checkedCnt + 1 : checkedCnt - 1);
            }
            e.stopPropagation();
        }
    };

    const handleTrClick = e => {
           if(e !== null && e !== undefined) {
            const id = e.target.parentElement.getAttribute("cbinfo");
            if (id !== null && id !== undefined) {
                const { lists, checked } = checkSortedList(sortedList, id);
                setSortedList(lists);
                setCheckedCnt(checked ? checkedCnt + 1 : checkedCnt - 1);
            }
            e.stopPropagation();
        }
    };

    const handleThClick = useCallback(key => {
        let changeDirection = "asc";

        if (sortKey === key && sortDirection === "asc") {
            changeDirection = "desc";
        }

        // changed to use lodash
        /*
        const list = sortedList.sort((a, b) => {
            const preVal = a[key].toLowerCase();
            const nextVal = b[key].toLowerCase();

            if (changeDirection === "asc") {
                return preVal.localeCompare(nextVal, "en", { numeric: true });
            } else {
                return nextVal.localeCompare(preVal, "en", { numeric: true });
            }
        });
        */
        setSortedList(sortedKeyDirectionList(sortedList, key, changeDirection));
        setSortKey(key);
        setSortDirection(changeDirection);
    },[sortedList, sortKey, sortDirection]);

    const handlePageChange = useCallback(page => {
        setCurrentPage(page);
    }, []);

    const handleSelectBoxChange = useCallback(value => {
        const startIndex = (currentPage - 1) * pageSize === 0 ? 1 : (currentPage - 1) * pageSize + 1;
        setPageSize(parseInt(value));
        setCurrentPage(Math.ceil(startIndex / parseInt(value)));
    }, [pageSize, currentPage]);

    const sortIconRender = name => {
        const style = "sort-icon";
        return sortKey === name ? style + " sort-active " + sortDirection : style;
    };

    const checkAllFileList = () => {
        if(!allItemChecked) {
            setSortedList(checkAllSortedList(sortedList, true))
            setCheckedCnt(sortedList.length);
        } else {
            setSortedList(checkAllSortedList(sortedList, false))
            setCheckedCnt(0);
        }
    }

    if (responseListCnt === 0) {
        return (
            <Card className="ribbon-wrapper filelist-card">
                <CardBody className="filelist-card-body">
                    <div className="ribbon ribbon-clip ribbon-info">File</div>
                    <div className="filelist-no-search">
                        <p>
                            <FontAwesomeIcon icon={faExclamationCircle} size="7x" />
                        </p>
                        <p>{requestCompletedDate === "" ? "Search has not started yet." : "File not found."}</p>
                    </div>
                </CardBody>
            </Card>
        );
    } else {
        return (
            <>
                <CreateModal
                    isConfirm={modalInfo.isDownloadConfirm}
                    isStart={modalInfo.isDownloadStart}
                    isCancel={modalInfo.isDownloadCancel}
                    isComplete={modalInfo.isDownloadComplete}
                    isError={modalInfo.isDownloadError}
                    errorMsg={modalInfo.downloadErrorMsg}
                    confirmClose={closeDownloadConfirm}
                    cancelClose={closeDownloadCancel}
                    completeClose={closeDownloadComplete}
                    errorClose={closeDownloadError}
                    confirmAction={openDownloadStart}
                    startAction={openDownloadCancel}
                    completeAction={closeDownloadComplete}
                    downStatus={statusDetail}
                />
                <Card className="ribbon-wrapper filelist-card">
                    <CardBody className="filelist-card-body">
                        <div className="ribbon ribbon-clip ribbon-info">File</div>
                        <Table className="vftp-sss">
                            <thead>
                                <tr>
                                    <th>
                                        <div>
                                            <ButtonToggle
                                                outline
                                                size="sm"
                                                color="info"
                                                className={"filelist-btn filelist-btn-toggle" + (allItemChecked ? " active" : "")}
                                                onClick={checkAllFileList}
                                            >
                                                All
                                            </ButtonToggle>
                                        </div>
                                    </th>
                                    <th onClick={() => handleThClick("machineName")}>
                                        <span className="sortLabel-root">
                                            Machine
                                            <span className={sortIconRender("machineName")}>➜</span>
                                        </span>
                                    </th>
                                    <th onClick={() => handleThClick("fileName")}>
                                        <span className="sortLabel-root">
                                            File Name
                                            <span className={sortIconRender("fileName")}>➜</span>
                                        </span>
                                    </th>
                                    <th onClick={() => handleThClick("fileSize")}>
                                        <span className="sortLabel-root">
                                            Size
                                            <span className={sortIconRender("fileSize")}>➜</span>
                                        </span>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <CreateFileList
                                    fileList={filePaginate(sortedList, currentPage, pageSize)}
                                    downloadCnt={checkedCnt}
                                    trClick={handleTrClick}
                                    checkboxClick={handleCheckboxClick}
                                />
                            </tbody>
                        </Table>
                    </CardBody>
                    <RenderPagination
                        pageSize={pageSize}
                        itemsCount={sortedList.length}
                        currentPage={currentPage}
                        onPageChange={handlePageChange}
                        className="custom-pagination"
                    />
                    <div className="filelist-info-area">
                        <label>{checkedCnt} File Selected</label>
                    </div>
                    <div className="filelist-item-area">
                        <div className="func-section">
                            <div>
                                <input
                                    type="checkbox"
                                    id="search-toggle"
                                    checked={onSearch}
                                    onChange={() => {
                                        setOnSearch(!onSearch)
                                        setQuery("");
                                    }}
                                />
                                <label htmlFor="search-toggle" className="search-toggle">
                                    <FontAwesomeIcon icon={faSearch} />
                                </label>
                            </div>
                            <div className={"input-section" + (onSearch ? " show" : "")}>
                                <Input
                                    type="text"
                                    tabIndex={-1}
                                    placeholder="Enter the file name to search."
                                    value={query}
                                    onChange={(e) => {
                                        setQuery(e.target.value);
                                        setCurrentPage(1);
                                    }}
                                />
                            </div>
                        </div>
                        <label>Rows per page : </label>
                        <Select
                            defaultValue={10}
                            value={pageSize}
                            onChange={handleSelectBoxChange}
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
                            onClick={openDownloadConfirm}
                        >
                            Download
                        </Button>
                    </div>
                </Card>
            </>
        );
    }
};

export default RSSvftpFilelist;

const CreateFileList = React.memo(
    ({ fileList, trClick, checkboxClick }) => {
        return fileList.map(file => {
            return (
                <tr key={file.index} onClick={trClick} cbinfo={file.index}>
                    <td>
                        <div className="custom-control custom-checkbox">
                            <input
                                type="checkbox"
                                className="custom-control-input"
                                id={file.index}
                                value={file.fileName}
                                checked={file.checked}
                                onChange={checkboxClick}
                            />
                            <label className="custom-control-label filelist-label" htmlFor={file.index}/>
                        </div>
                    </td>
                    <td>{file.machineName}</td>
                    <td><FontAwesomeIcon icon={faFileAlt} /> {file.fileName}</td>
                    <td>{API.bytesToSize(file.fileSize)}</td>
                </tr>
            );
        });
    },
    // propsCompare
);

const CreateModal = React.memo(
    ({
         isConfirm,
         isStart,
         isCancel,
         isComplete,
         isError,
         errorMsg,
         confirmClose,
         cancelClose,
         completeClose,
         errorClose,
         confirmAction,
         startAction,
         completeAction,
         downStatus
     }) => {
        if (isConfirm) {
            return (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                >
                    <div className="Custom-modal-overlay" />
                    <div className="Custom-modal">
                        <div className="content-without-title">
                            <p><FontAwesomeIcon icon={faDownload} size="8x" /></p>
                            <p>Do you want to download the selected file?</p>
                        </div>
                        <div className="button-wrap">
                            <button className="secondary form-type left-btn" onClick={confirmAction}>
                                Download
                            </button>
                            <button className="secondary form-type right-btn" onClick={confirmClose}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </ReactTransitionGroup>
            );
        } else if (isStart) {
            return (
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
                                { API.convertDownloadMsg(downStatus.status) }
                            </p>
                            {(downStatus.totalFiles > 0)  &&
	                            <p className="no-margin-no-padding">
	                                File: ({downStatus.downloadedFiles}/{downStatus.totalFiles}) <br/>
	                                DownloadedSize: ({downStatus.downloadSize? API.bytesToSize(downStatus.downloadSize) : "0 Byte"} /{API.bytesToSize(downStatus.totalSize)})
	                            </p>
                            }
                        </div>
                        <div className="button-wrap">
                            <button className="secondary alert-type" onClick={startAction}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </ReactTransitionGroup>
            );
        } else if (isCancel) {
            return (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                >
                    <div className="Custom-modal-overlay" />
                    <div className="Custom-modal">
                        <div className="content-without-title">
                            <p><FontAwesomeIcon icon={faBan} size="8x" /></p>
                            <p>Are you sure you want to cancel the download?</p>
                        </div>
                        <div className="button-wrap">
                            <button className="secondary form-type left-btn" onClick={() => cancelClose("OK")}>
                                Yes
                            </button>
                            <button className="secondary form-type right-btn" onClick={() => cancelClose("Cancel")}>
                                No
                            </button>
                        </div>
                    </div>
                </ReactTransitionGroup>
            );
        } else if (isComplete) {
            return (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                >
                    <div className="Custom-modal-overlay" />
                    <div className="Custom-modal">
                        <div className="content-without-title">
                            <p><FontAwesomeIcon icon={faChevronCircleDown} size="8x" /></p>
                            <p>Download Complete!</p>
                        </div>
                        <div className="button-wrap">
                            <button className="secondary form-type left-btn" onClick={() => completeAction(true)}>
                                Save
                            </button>
                            <button className="secondary form-type right-btn" onClick={() => completeClose(false)}>
                                Cancel
                            </button>
                        </div>
                    </div>
                </ReactTransitionGroup>
            );
        } else if (isError) {
            return (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                >
                    <div className="Custom-modal-overlay" />
                    <div className="Custom-modal">
                        <div className="content-without-title">
                            <p><FontAwesomeIcon icon={faExclamationCircle} size="8x" /></p>
                            <p>{errorMsg}</p>
                        </div>
                        <div className="button-wrap">
                            <button className="secondary alert-type" onClick={errorClose}>
                                Close
                            </button>
                        </div>
                    </div>
                </ReactTransitionGroup>
            );
        } else {
            return (
                <ReactTransitionGroup
                    transitionName={"Custom-modal-anim"}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                />
            );
        }
    }
);

function sortedAllList(lists, query, sortKey, sortDirection) {
    return sortedKeyDirectionList(sortedQueryList(lists, query), sortKey, sortDirection);
}

function sortedQueryList(lists, query) {
    return query ? lists.filter((list) => {
        return list.fileName.toLowerCase().includes(query.toLowerCase());
    }) : lists;
}

function sortedKeyDirectionList(lists, sortKey, sortDirection) {
    return _.orderBy(lists, sortKey, sortDirection);
}

function checkSortedList(lists, idx) {
    let checked = true;
    return {
        lists: produce(lists, draft => {
            const findIdx = lists.findIndex(item => item.index === +idx);
            checked = !draft[findIdx].checked;
            draft[findIdx].checked = checked;
        }),
        checked
    }
}

function checkAllSortedList(lists, check = true) {
    return produce(lists, draft => {
        for(const item of draft) {
            item.checked = check
        }
    })
}