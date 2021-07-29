import React, {useState, useCallback} from "react";
import { Card, CardBody, Col, FormGroup, Button, Input, CustomInput,
    UncontrolledPopover, PopoverBody, PopoverHeader, Table } from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {faTrash, faPen, faExclamationCircle} from "@fortawesome/free-solid-svg-icons";
import ReactTransitionGroup from "react-addons-css-transition-group";
import { connect } from "react-redux";
import {bindActionCreators} from "redux";
import { propsCompare, invalidCheckVFTP, setCurrentCommand } from "../../Common/CommonFunction";
import * as commandActions from "../../../modules/command";
import services from "../../../services";
import * as API from "../../../api";
import * as Define from "../../../define";

const UNIQUE_COMMAND = "none";
const modalType = { NEW: 1, EDIT: 2 };
const inputTitleMsg = {
    COMPAT: "Command (Context-Unit Name-Data Item)",
    SSS: "Command (Context)"
};

//const RSScommandlist = ({ cmdType, dbCommand, commandActions, states }) => {
const RSScommandlist = ({ cmdType, dbCommand, commandActions }) => {
    const commandList = API.vftpConvertDBCommand(dbCommand.get("lists").toJS());
    const [selectCommand, setSelectCommand] = useState(-1);
    const [actionId, setActionId] = useState(-1);
    const [currentDataType, setCurrentDataType] = useState("");
    const [currentContext, setCurrentContext] = useState("");
    const [errorMsg, setErrorMsg] = useState("");
    const [isNewOpen, setIsNewOpen] = useState(false);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isDeleteOpen, setIsDeleteOpen] = useState(false);
    const [isErrorOpen, setIsErrorOpen] = useState(false);
    const [openedModal, setOpenedModal] = useState("");

    /*
    const [selectCommand, setSelectCommand] = useState(states.selectCommand);
    const [actionId, setActionId] = useState(states.actionId);
    const [currentDataType, setCurrentDataType] = useState(states.currentDateType);
    const [currentContext, setCurrentContext] = useState(states.currentContext);
    const [errorMsg, setErrorMsg] = useState(states.errorMsg);
    const [isNewOpen, setIsNewOpen] = useState(states.isNewOpen);
    const [isEditOpen, setIsEditOpen] = useState(states.isEditOpen);
    const [isDeleteOpen, setIsDeleteOpen] = useState(states.isDeleteOpen);
    const [isErrorOpen, setIsErrorOpen] = useState(states.isErrorOpen);
    const [openedModal, setOpenedModal] = useState(states.openedModal);
     */

    const handleCommandChange = useCallback(id => {
        commandActions.commandCheckOnlyOneList(id);
        setSelectCommand(id);
    }, []);

    const openAddModal = useCallback(() => {
        setIsNewOpen(true);
    }, []);

    const openEditModal = useCallback((id, value) => {
        setIsEditOpen(true);
        setActionId(id);

        if (cmdType === Define.PLAN_TYPE_VFTP_COMPAT) {
            setCurrentContext(value);
        } else {
            const hyphenCheck = value.match(/-/g);
            if (hyphenCheck !== null) {
                setCurrentDataType(value.slice(0, value.indexOf("-")));
                setCurrentContext(value.slice(value.indexOf("-") + 1, value.length));
            } else {
                setCurrentDataType(value);
            }
        }
    }, []);

    const openDeleteModal = useCallback(id => {
        setIsDeleteOpen(true);
        setActionId(id);
    }, []);

    const closeAddModal = useCallback(() => {
        setIsNewOpen(false);
        setErrorMsg("");
        setCurrentDataType("");
        setCurrentContext("");
    }, []);

    const closeEditModal = useCallback(() => {
        setIsEditOpen(false);
        setActionId(-1);
        setErrorMsg("");
        setCurrentDataType("");
        setCurrentContext("");
    }, []);

    const closeDeleteModal = useCallback(() => {
        setIsDeleteOpen(false);
        setActionId(-1);
    }, []);

    const closeErrorModal = useCallback(() => {
        setIsErrorOpen(false);
        setTimeout(() => {
            if (openedModal === modalType.NEW) {
                setIsNewOpen(true);
            } else {
                setIsEditOpen(true);
            }
        }, 500);
    }, [openedModal]);

    const onContextChange = useCallback(e => { setCurrentContext(e.target.value); }, []);
    const onDataTypeChange = useCallback(e=> { setCurrentDataType(e.target.value); }, []);

    const addCommand = useCallback(async () => {
        if (invalidCheckVFTP(cmdType, setErrorMsg, () => setOpenedModal(modalType.NEW), currentContext, currentDataType)) {
            setIsNewOpen(false);
            setTimeout(() => { setIsErrorOpen(true); }, 500);
        } else {
            const currentCommand =
                cmdType === Define.PLAN_TYPE_VFTP_COMPAT ? currentContext :
                    currentContext === "" ? currentDataType : currentDataType + "-" + currentContext;
            const duplicateArray = commandList.filter(command => command.cmd_name.toLowerCase() === currentCommand.toLowerCase());

            if (duplicateArray.length !== 0
                || (cmdType === Define.PLAN_TYPE_VFTP_COMPAT && currentCommand.toLowerCase() === UNIQUE_COMMAND)) {
                setErrorMsg("This command is duplicate.");
                setOpenedModal(modalType.NEW);
                setIsNewOpen(false);
                setTimeout(() => { setIsErrorOpen(true); }, 500);
            } else {
                const addData = {
                    cmd_name: setCurrentCommand(cmdType, currentDataType, currentContext),
                    cmd_type: cmdType
                }
                try {
                    const res = await services.axiosAPI.requestPost("/rss/api/vftp/command", addData);
                    const {data: {id}} = res;
                    if (res.status === 200) {
                        await commandActions.commandLoadList(`/rss/api/vftp/command?type=${cmdType}`);
                        await commandActions.commandCheckOnlyOneList(id);
                        setSelectCommand(id);
                    }
                } catch (e) {
                    // 에러 처리
                    console.error(e);
                }

                setIsNewOpen(false);
                setCurrentContext("");
                setCurrentDataType("");
                setErrorMsg("");
                setOpenedModal("");
            }
        }
    }, [commandList, currentContext, currentDataType]);

    const editCommand = useCallback(async () => {
        if (invalidCheckVFTP(cmdType, setErrorMsg, () => setOpenedModal(modalType.EDIT), currentContext, currentDataType)) {
            setIsEditOpen(false);
            setTimeout(() => { setIsErrorOpen(true); }, 500);
        } else {
            const currentCommand =
                cmdType === Define.PLAN_TYPE_VFTP_COMPAT ? currentContext :
                    currentContext === "" ? currentDataType : currentDataType + "-" + currentContext;
            const duplicateArray = commandList.filter(command => command.cmd_name.toLowerCase() === currentCommand.toLowerCase());

            if ((duplicateArray.length !== 0 && duplicateArray[0].id !== actionId)
                || (cmdType === Define.PLAN_TYPE_VFTP_COMPAT && currentCommand.toLowerCase() === UNIQUE_COMMAND)) {
                setErrorMsg("This command is duplicate.");
                setOpenedModal(modalType.EDIT);
                setIsEditOpen(false);
                setTimeout(() => { setIsErrorOpen(true); }, 500);
            } else {
                const editItem = { cmd_name: setCurrentCommand(cmdType, currentDataType, currentContext) };
                try {
                    const res = await services.axiosAPI.requestPut(`/rss/api/vftp/command/${actionId}`, editItem);
                    if(res.status === 200) {
                        await commandActions.commandLoadList(`/rss/api/vftp/command?type=${cmdType}`);
                        if (selectCommand !== -1) {
                            await commandActions.commandCheckOnlyOneList(selectCommand);
                        }
                    }
                } catch (e) {
                    // 에러 처리
                    console.error(e);
                }

                setIsEditOpen(false);
                setActionId(-1);
                setCurrentContext("");
                setCurrentDataType("");
                setErrorMsg("");
                setOpenedModal("");
            }
        }
    }, [commandList, actionId, selectCommand, currentDataType, currentContext]);

    const deleteCommand = useCallback(async () => {
        try {
            const res = await services.axiosAPI.requestDelete(`/rss/api/vftp/command/${actionId}`);

            if (res.status === 200) {
                //await commandActions.commandInit();
                await commandActions.commandLoadList(`/rss/api/vftp/command?type=${cmdType}`);
                if (commandList.length === 0 || actionId === selectCommand) {
                    setSelectCommand(-1);
                    setActionId(-1);
                }

                if (selectCommand !== -1 && actionId !== selectCommand) {
                    await commandActions.commandCheckOnlyOneList(selectCommand);
                }
            }
        } catch (e) {
            // 에러 처리
            console.error(e);
        }

        setIsDeleteOpen(false);
    }, [actionId, selectCommand]);

    return (
        <>
            <Card className="ribbon-wrapper catlist-card command-list manual">
                <CardBody className="custom-scrollbar manual-card-body">
                    <div className="ribbon ribbon-clip ribbon-secondary">Command</div>
                    <Col>
                        <FormGroup className="catlist-form-group">
                            <ul>
                                {cmdType === "vftp_compat" &&
                                    <li>
                                        <CustomInput
                                            type="radio"
                                            id={-1}
                                            name="notUse"
                                            label="none"
                                            checked={selectCommand === -1}
                                            onChange={() => handleCommandChange(-1)}
                                        />
                                    </li>
                                }
                                <CreateCommandList
                                    type={cmdType}
                                    commandList={commandList}
                                    commandChanger={handleCommandChange}
                                    editModal={openEditModal}
                                    deleteModal={openDeleteModal}
                                />
                            </ul>
                        </FormGroup>
                    </Col>
                    <div className="card-btn-area">
                        <Button
                            outline
                            size="sm"
                            color="info"
                            className="catlist-btn"
                            onClick={openAddModal}
                        >
                            Add
                        </Button>
                    </div>
                </CardBody>
            </Card>
            <CreateModal
                listType={cmdType}
                dataType={currentDataType}
                context={currentContext}
                dataTypeChanger={onDataTypeChange}
                contextChanger={onContextChange}
                newOpen={isNewOpen}
                editOpen={isEditOpen}
                deleteOpen={isDeleteOpen}
                errorOpen={isErrorOpen}
                actionNew={addCommand}
                actionEdit={editCommand}
                actionDelete={deleteCommand}
                closeNew={closeAddModal}
                closeEdit={closeEditModal}
                closeDelete={closeDeleteModal}
                closeError={closeErrorModal}
                msg={errorMsg}
            />
        </>
    );
};

const CreateModal = React.memo(({ ...props }) => {
    const { listType, dataType, context, dataTypeChanger, contextChanger, newOpen, editOpen, deleteOpen, errorOpen,
            actionNew, actionEdit, actionDelete, closeNew, closeEdit, closeDelete, closeError, msg } = props;

    if (newOpen) {
        return (
            <ReactTransitionGroup
                transitionName={"Custom-modal-anim"}
                transitionEnterTimeout={200}
                transitionLeaveTimeout={200}
            >
                <div className="Custom-modal-overlay" onClick={closeNew} />
                <div className="Custom-modal command">
                    <p className="title">Add</p>
                    <div className="content-with-title">
                        <FormGroup className={"command-input-modal" + (listType === Define.PLAN_TYPE_VFTP_COMPAT ? " hidden" : "")}>
                            <label className="manual">Data type</label>
                            <Input
                                type="text"
                                id="datatype"
                                placeholder="Enter data type"
                                className="manual"
                                value={dataType}
                                onChange={dataTypeChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="datatype"
                                trigger="hover"
                                className="command-list"
                                fade={false}
                                delay={{ show: 300, hide: 0 }}
                            >
                                <PopoverHeader>How to enter</PopoverHeader>
                                <PopoverBody>
                                    <div className="explain-area">
                                        <p className="title">「Data Type」</p>
                                        <p>RAW data: IP_AS_RAW</p>
                                        <p>RAW data of measurement error: IP_AS_RAW_ERR</p>
                                        <p>Image data: IP_AS_BMP</p>
                                        <p>Image data of measurement error: IP_AS_BMP_ERR</p>
                                    </div>
                                </PopoverBody>
                            </UncontrolledPopover>
                        </FormGroup>
                        <FormGroup className="command-input-modal">
                            <label className="manual">
                                {listType === Define.PLAN_TYPE_VFTP_COMPAT ? inputTitleMsg.COMPAT : inputTitleMsg.SSS}
                            </label>
                            <Input
                                type="text"
                                id="command"
                                placeholder="Enter command"
                                className="manual"
                                value={context}
                                onChange={contextChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="command"
                                trigger="hover"
                                className="command-list"
                                fade={false}
                                delay={{ show: 300, hide: 0 }}
                            >
                                <PopoverHeader>How to enter</PopoverHeader>
                                <PopoverBody>
                                    <div className="explain-area">
                                        <p className="title">「Context」</p>
                                        <Table>
                                            <thead>
                                            <tr>
                                                <th>Contents</th>
                                                <th>Identifier</th>
                                                <th>Specified input</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr>
                                                <td>Device Name</td>
                                                <td>DE</td>
                                                <td>value(String(Maximum: 8 Bytes))</td>
                                            </tr>
                                            <tr>
                                                <td>Process Name</td>
                                                <td>PR</td>
                                                <td>value(String(Maximum: 8 Bytes))</td>
                                            </tr>
                                            <tr>
                                                <td>Lot ID</td>
                                                <td>LO</td>
                                                <td>value(String(Maximum: 32 Bytes))</td>
                                            </tr>
                                            <tr>
                                                <td>Plate Number</td>
                                                <td>P</td>
                                                <td>value, range</td>
                                            </tr>
                                            <tr>
                                                <td>Shot Number</td>
                                                <td>S</td>
                                                <td>value, range</td>
                                            </tr>
                                            <tr>
                                                <td>Mark Number</td>
                                                <td>M</td>
                                                <td>value, range</td>
                                            </tr>
                                            <tr>
                                                <td>Glass ID</td>
                                                <td>G</td>
                                                <td>value(String(Maximum: 16 Bytes))</td>
                                            </tr>
                                            </tbody>
                                        </Table>
                                        <p>
                                            Example:
                                            DE_XXX_PR_XXX_LO_XXXX_P_XXXX_XXXX_S_XX_XX_M_X_X_G_XXXX
                                        </p>
                                    </div>
                                    { listType === Define.PLAN_TYPE_VFTP_COMPAT ?
                                        (
                                            <>
                                                <div className="explain-area">
                                                    <p className="title">「Unit Name」</p>
                                                    <p>Console: CONS</p>
                                                    <p>Main Unit: MINC</p>
                                                    <p>TVAA: TVAA</p>
                                                    <p>Example: U_CONS_MINC_TVAA</p>
                                                </div>
                                                <div className="explain-area">
                                                    <p className="title">「Data Item」</p>
                                                    <p>Enter the label name assigned to each data content.</p>
                                                    <p>You can specify more than one label name.</p>
                                                    <p>※Label: A string of 6 characters or less to register when logging each data.</p>
                                                    <p>Example: L_LABEL1_LABEL2</p>
                                                </div>
                                            </>
                                        ) : (<></>)
                                    }
                                </PopoverBody>
                            </UncontrolledPopover>
                        </FormGroup>
                    </div>
                    <div className="button-wrap">
                        <button className="primary form-type left-btn" onClick={actionNew}>
                            Add
                        </button>
                        <button className="primary form-type right-btn" onClick={closeNew}>
                            Cancel
                        </button>
                    </div>
                </div>
            </ReactTransitionGroup>
        );
    } else if (editOpen) {
        return (
            <ReactTransitionGroup
                transitionName={"Custom-modal-anim"}
                transitionEnterTimeout={200}
                transitionLeaveTimeout={200}
            >
                <div className="Custom-modal-overlay" onClick={closeEdit} />
                <div className="Custom-modal command">
                    <p className="title">Edit</p>
                    <div className="content-with-title">
                        <FormGroup className={"command-input-modal" + (listType === Define.PLAN_TYPE_VFTP_COMPAT ? " hidden" : "")}>
                            <label className="manual">Data type</label>
                            <Input
                                type="text"
                                id="datatype"
                                placeholder="Enter data type"
                                className="manual"
                                value={dataType}
                                onChange={dataTypeChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="datatype"
                                trigger="hover"
                                className="command-list"
                                fade={false}
                                delay={{ show: 300, hide: 0 }}
                            >
                                <PopoverHeader>How to enter</PopoverHeader>
                                <PopoverBody>
                                    <div className="explain-area">
                                        <p className="title">「Data Type」</p>
                                        <p>RAW data: IP_AS_RAW</p>
                                        <p>RAW data of measurement error: IP_AS_RAW_ERR</p>
                                        <p>Image data: IP_AS_BMP</p>
                                        <p>Image data of measurement error: IP_AS_BMP_ERR</p>
                                    </div>
                                </PopoverBody>
                            </UncontrolledPopover>
                        </FormGroup>
                        <FormGroup className="command-input-modal">
                            <label className="manual">
                                {listType === Define.PLAN_TYPE_VFTP_COMPAT ? inputTitleMsg.COMPAT : inputTitleMsg.SSS}
                            </label>
                            <Input
                                type="text"
                                id="command"
                                placeholder="Enter command"
                                className="manual"
                                value={context}
                                onChange={contextChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="command"
                                trigger="hover"
                                className="command-list"
                                fade={false}
                                delay={{ show: 300, hide: 0 }}
                            >
                                <PopoverHeader>How to enter</PopoverHeader>
                                <PopoverBody>
                                    <div className="explain-area">
                                        <p className="title">「Context」</p>
                                        <Table>
                                            <thead>
                                            <tr>
                                                <th>Contents</th>
                                                <th>Identifier</th>
                                                <th>Specified input</th>
                                            </tr>
                                            </thead>
                                            <tbody>
                                            <tr>
                                                <td>Device Name</td>
                                                <td>DE</td>
                                                <td>value(String(Maximum: 8 Bytes))</td>
                                            </tr>
                                            <tr>
                                                <td>Process Name</td>
                                                <td>PR</td>
                                                <td>value(String(Maximum: 8 Bytes))</td>
                                            </tr>
                                            <tr>
                                                <td>Lot ID</td>
                                                <td>LO</td>
                                                <td>value(String(Maximum: 32 Bytes))</td>
                                            </tr>
                                            <tr>
                                                <td>Plate Number</td>
                                                <td>P</td>
                                                <td>value, range</td>
                                            </tr>
                                            <tr>
                                                <td>Shot Number</td>
                                                <td>S</td>
                                                <td>value, range</td>
                                            </tr>
                                            <tr>
                                                <td>Mark Number</td>
                                                <td>M</td>
                                                <td>value, range</td>
                                            </tr>
                                            <tr>
                                                <td>Glass ID</td>
                                                <td>G</td>
                                                <td>value(String(Maximum: 16 Bytes))</td>
                                            </tr>
                                            </tbody>
                                        </Table>
                                        <p>
                                            Example:
                                            DE_XXX_PR_XXX_LO_XXXX_P_XXXX_XXXX_S_XX_XX_M_X_X_G_XXXX
                                        </p>
                                    </div>
                                    { listType === Define.PLAN_TYPE_VFTP_COMPAT ?
                                        (
                                            <>
                                                <div className="explain-area">
                                                    <p className="title">「Unit Name」</p>
                                                    <p>Console: CONS</p>
                                                    <p>Main Unit: MINC</p>
                                                    <p>TVAA: TVAA</p>
                                                    <p>Example: U_CONS_MINC_TVAA</p>
                                                </div>
                                                <div className="explain-area">
                                                    <p className="title">「Data Item」</p>
                                                    <p>Enter the label name assigned to each data content.</p>
                                                    <p>You can specify more than one label name.</p>
                                                    <p>※Label: A string of 6 characters or less to register when logging each data.</p>
                                                    <p>Example: L_LABEL1_LABEL2</p>
                                                </div>
                                            </>
                                        ) : (<></>)
                                    }
                                </PopoverBody>
                            </UncontrolledPopover>
                        </FormGroup>
                    </div>
                    <div className="button-wrap">
                        <button className="primary form-type left-btn" onClick={actionEdit}>
                            Save
                        </button>
                        <button className="primary form-type right-btn" onClick={closeEdit}>
                            Cancel
                        </button>
                    </div>
                </div>
            </ReactTransitionGroup>
        );
    } else if (deleteOpen) {
        return (
            <ReactTransitionGroup
                transitionName={"Custom-modal-anim"}
                transitionEnterTimeout={200}
                transitionLeaveTimeout={200}
            >
                <div className="Custom-modal-overlay" onClick={closeDelete}/>
                <div className="Custom-modal">
                    <div className="content-without-title">
                        <p><FontAwesomeIcon icon={faTrash} size="8x"/></p>
                        <p>Do you want to delete this command?</p>
                    </div>
                    <div className="button-wrap">
                        <button
                            className="primary form-type left-btn"
                            onClick={actionDelete}
                        >
                            Delete
                        </button>
                        <button
                            className="primary form-type right-btn"
                            onClick={closeDelete}
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            </ReactTransitionGroup>
        );
    } else if (errorOpen) {
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
                        <p>{msg}</p>
                    </div>
                    <div className="button-wrap">
                        <button className="primary alert-type" onClick={closeError}>
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
}, propsCompare);

const CreateCommandList = React.memo(({ type, commandList, commandChanger, editModal, deleteModal }) => {
    if (type === Define.PLAN_TYPE_VFTP_SSS && commandList.length === 0) {
        return (
            <div className="no-search-genre">
                <p><FontAwesomeIcon icon={faExclamationCircle} size="8x" /></p>
                <p>No registered command.</p>
            </div>
        );
    } else {
        return (
            <>
                {commandList.map((command, index) => {
                    return (
                        <li key={index}>
                            <CustomInput
                                type="radio"
                                id={command.id}
                                name={command.cmd_name}
                                label={command.cmd_name}
                                checked={command.checked}
                                onChange={() => commandChanger(command.id)}
                            />
                            <span className="icon" onClick={() => deleteModal(command.id)}>
                            <FontAwesomeIcon icon={faTrash}/>
                        </span>
                            <span className="icon" onClick={() => editModal(command.id, command.cmd_name)}>
                            <FontAwesomeIcon icon={faPen}/>
                        </span>
                        </li>
                    );
                })}
            </>
        );
    }
}, propsCompare);

export default connect(
  (state) => ({
      dbCommand: state.command.get('command'),
  }),
  (dispatch) => ({
      commandActions: bindActionCreators(commandActions, dispatch),
  })
)(RSScommandlist);
