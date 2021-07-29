import React, {useCallback, useEffect, useState} from "react";
import {Button, ButtonToggle, Col, FormGroup, Input, UncontrolledPopover,
    PopoverBody, PopoverHeader, Table} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faExclamationCircle, faPen, faSearch, faTrash} from "@fortawesome/free-solid-svg-icons";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import {propsCompare, invalidCheckVFTP, setCurrentCommand} from "../../Common/CommonFunction";
import services from "../../../services";
import * as commandActions from "../../../modules/command";
import * as Define from "../../../define";

const modalType = { NEW: 1, EDIT: 2 };
const UNIQUE_COMMAND = "none";
const inputTitleMsg = {
    COMPAT: "Command (Context-Unit Name-Data Item)",
    SSS: "Command (Context)"
};

//const RSSautoCommandList = ({ type, command, commandActions, autoPlan, states }) => {
const RSSautoCommandList = ({ type, command, commandActions, autoPlan }) => {
    const commandList = command.get("lists").toJS();
    const [query, setQuery] = useState("");
    const [showSearch, setShowSearch] = useState(false);
    const [actionId, setActionId] = useState("");
    const [currentDataType, setCurrentDataType] = useState("");
    const [currentContext, setCurrentContext] = useState("");
    const [errorMsg, setErrorMsg] = useState("");
    const [isNewOpen, setIsNewOpen] = useState(false);
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isDeleteOpen, setIsDeleteOpen] = useState(false);
    const [isErrorOpen, setIsErrorOpen] = useState(false);
    const [openedModal, setOpenedModal] = useState("");

    /*
    const [query, setQuery] = useState(states.query);
    const [showSearch, setShowSearch] = useState(states.showSearch);
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

    const handleSearchToggle = useCallback(() => {
        setShowSearch(!showSearch);
        setQuery("");
        //console.log("handleSearchToggle called!");
    }, [showSearch]);

    const selectItem = useCallback((check) => {
        commandActions.commandCheckAllList({ check: check, query: query });
        //console.log("selectItem called!");
    }, [query]);

    const handleCheckboxClick = useCallback(e => {
        commandActions.commandCheckList(parseInt(e.target.id));
        //console.log("handleCheckboxClick called!");
    }, []);

    const handleSearch = useCallback(e => {
        setQuery(e.target.value);
        //console.log("handleSearch called!");
    }, []);

    const addCommand = useCallback(async () => {
        //console.log("addCommand called!");
        if (invalidCheckVFTP(type, setErrorMsg, () => setOpenedModal(modalType.NEW), currentContext, currentDataType)) {
            setIsNewOpen(false);
            setTimeout(() => { setIsErrorOpen(true); }, 500);
        } else {
            const currentCommand = setCurrentCommand(type, currentDataType, currentContext);
            const duplicateArray = commandList.filter(command => command.cmd_name.toLowerCase() === currentCommand.toLowerCase());

            if (duplicateArray.length !== 0
                || (type === Define.PLAN_TYPE_VFTP_COMPAT && currentContext.toLowerCase() === UNIQUE_COMMAND)) {
                setErrorMsg("This command is duplicate.");
                setOpenedModal(modalType.NEW);
                setIsNewOpen(false);
                setTimeout(() => { setIsErrorOpen(true); }, 500);
            } else {
                const commandItem = { cmd_name: currentCommand, cmd_type: type };

                try {
                    const res = await services.axiosAPI.requestPost("/rss/api/vftp/command", commandItem);
                    console.log(res);

                    if (res.status === 200) {
                        const { data: id } = res;
                        await commandActions.commandLoadList("/rss/api/vftp/command?type=" + type);
                        if (type === Define.PLAN_TYPE_VFTP_COMPAT) {
                            await commandActions.commandAddNotUse();
                        }
                        commandActions.commandCheckList(parseInt(id));
                    }
                } catch (e) { console.log(e.message()); }

                setIsNewOpen(false);
                setCurrentContext("");
                setCurrentDataType("");
                setErrorMsg("");
                setOpenedModal("");
            }
        }
    }, [commandList, currentDataType, currentContext]);

    const saveCommand = useCallback(async () => {
        //console.log("saveCommand called!");
        if (invalidCheckVFTP(type, setErrorMsg, () => setOpenedModal(modalType.EDIT), currentContext, currentDataType)) {
            setIsEditOpen(false);
            setTimeout(() => { setIsErrorOpen(true); }, 500);
        } else {
            const currentCommand = setCurrentCommand(type, currentDataType, currentContext);
            const duplicateArray = commandList.filter(command => {
                if (command.id !== actionId) {
                    return command.cmd_name.toLowerCase() === currentCommand.toLowerCase();
                }
            });

            if (duplicateArray.length !== 0 || (type === Define.PLAN_TYPE_VFTP_COMPAT && currentContext.toLowerCase() === UNIQUE_COMMAND)) {
                setErrorMsg("This command is duplicate.");
                setOpenedModal(modalType.EDIT);
                setIsEditOpen(false);
                setTimeout(() => { setIsErrorOpen(true); }, 500);
            } else {
                const commandItem = { cmd_name: currentCommand };

                try {
                    const res = await services.axiosAPI.requestPut(`/rss/api/vftp/command/${actionId}`, commandItem);
                    console.log(res)

                    if (res.status === 200) {
                        await commandActions.commandLoadList("/rss/api/vftp/command?type=" + type);
                        if (type === Define.PLAN_TYPE_VFTP_COMPAT) {
                            await commandActions.commandAddNotUse();
                        }
                    }
                } catch (e) { console.log(e.message()); }

                setIsEditOpen(false);
                setActionId("");
                setCurrentContext("");
                setCurrentDataType("");
                setErrorMsg("");
                setOpenedModal("");
            }
        }
    }, [commandList, actionId, currentDataType, currentContext]);

    const deleteCommand = useCallback(async () => {
        //console.log("deleteCommand called!");
        try {
            const res = await services.axiosAPI.requestDelete(`/rss/api/vftp/command/${actionId}`);
            console.log(res);

            if (res.status === 200) {
                commandActions.commandDeleteItem({ id: actionId, planType: type });
                await commandActions.commandLoadList("/rss/api/vftp/command?type=" + type);
                if (type === Define.PLAN_TYPE_VFTP_COMPAT) {
                    await commandActions.commandAddNotUse();
                }
            }
        } catch (e) { console.log(e.message()); }

        setIsDeleteOpen(false);
        setActionId("");
    }, [commandList, actionId]);

    const onContextChange = useCallback(e => {
        setCurrentContext(e.target.value);
        //console.log("onContextChange called!");
    }, []);
    const onDataTypeChange = useCallback(e=> {
        setCurrentDataType(e.target.value);
        //console.log("onDataTypeChange called!");
    }, []);
    const openNewModal = useCallback(() => {
        setIsNewOpen(true);
        //console.log("openNewModal called!");
    }, []);

    const openEditModal = useCallback((id, value) => {
        //console.log("openEditModal called!");
        setIsEditOpen(true);
        setActionId(id);
        if (type === Define.PLAN_TYPE_VFTP_COMPAT) {
            setCurrentContext(value.replace("%s-%s-", ""));
        } else {
            if (value.endsWith("%s")) {
                setCurrentDataType(value.replace("-%s-%s", ""));
            } else {
                setCurrentDataType(value.split("-%s-%s-")[0]);
                setCurrentContext(value.split("-%s-%s-")[1]);
            }
        }
    }, []);

    const openDeleteModal = useCallback(id => {
        //console.log("openDeleteModal called!");
        setIsDeleteOpen(true);
        setActionId(id);
    }, []);

    const closeNewModal = useCallback(() => {
        //console.log("closeNewModal called!");
        setIsNewOpen(false);
        setCurrentDataType("");
        setCurrentContext("");
    }, []);

    const closeEditModal = useCallback(() => {
        //console.log("closeEditModal called!");
        setIsEditOpen(false);
        setActionId("");
        setCurrentDataType("");
        setCurrentContext("");
    }, []);

    const closeDeleteModal = useCallback(() => {
        //console.log("closeDeleteModal called!");
        setIsDeleteOpen(false);
        setActionId("");
    }, []);

    const closeErrorModal = useCallback(() => {
        //console.log("closeErrorModal called!");
        setIsErrorOpen(false);
        setTimeout(() => {
            if (openedModal === modalType.NEW) {
                setIsNewOpen(true);
            } else {
                setIsEditOpen(true);
            }
        }, 500);
    }, [openedModal]);

    useEffect(() => {
        const fetchData = async () => {
            const { commands } = autoPlan.toJS();
            await commandActions.commandCheckInit(commands);
        }
        fetchData();
    }, []);

    return (
        <>
            <div className="form-section targetlist">
                <Col className="pdl-10 pdr-0">
                    <div className="form-header-section">
                        <div className="form-title-section">
                            Command List
                            <p>Select a command from the list.</p>
                        </div>
                        <CreateButtonArea
                            isOpen={showSearch}
                            isChecked={isAllChecked(createFilteredList(commandList, query))}
                            searchToggler={handleSearchToggle}
                            searchText={query}
                            textChanger={handleSearch}
                            itemToggler={selectItem}
                            openModal={openNewModal}
                        />
                    </div>
                    <CreateCommandList
                        commandList={commandList}
                        checkHandler={handleCheckboxClick}
                        query={query}
                        editModal={openEditModal}
                        deleteModal={openDeleteModal}
                        type={type}
                    />
                </Col>
            </div>
            <CreateModal
                listType={type}
                dataType={currentDataType}
                context={currentContext}
                dataTypeChanger={onDataTypeChange}
                contextChanger={onContextChange}
                newOpen={isNewOpen}
                editOpen={isEditOpen}
                deleteOpen={isDeleteOpen}
                errorOpen={isErrorOpen}
                actionNew={addCommand}
                actionEdit={saveCommand}
                actionDelete={deleteCommand}
                closeNew={closeNewModal}
                closeEdit={closeEditModal}
                closeDelete={closeDeleteModal}
                closeError={closeErrorModal}
                msg={errorMsg}
            />
        </>
    );
};

const CreateModal = React.memo(({ ...props }) => {
    const { listType, dataType, context, dataTypeChanger, contextChanger, newOpen, editOpen, deleteOpen,
            errorOpen, actionNew, actionEdit, actionDelete, closeNew, closeEdit, closeDelete, closeError, msg } = props;

    if (newOpen) {
        return (
            <ReactTransitionGroup
                transitionName={"Custom-modal-anim"}
                transitionEnterTimeout={200}
                transitionLeaveTimeout={200}
            >
                <div className="Custom-modal-overlay" onClick={closeNew} />
                <div className="Custom-modal auto-plan-confirm-modal command">
                    <p className="title">Add</p>
                    <div className="content-with-title">
                        <FormGroup className={"command-input-modal" + (listType === Define.PLAN_TYPE_VFTP_COMPAT ? " hidden" : "")}>
                            <label>Data type</label>
                            <Input
                                type="text"
                                id="datatype"
                                placeholder="Enter data type"
                                value={dataType}
                                onChange={dataTypeChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="datatype"
                                trigger="hover"
                                className="command-list auto"
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
                            <label>
                                {listType === Define.PLAN_TYPE_VFTP_COMPAT ? inputTitleMsg.COMPAT : inputTitleMsg.SSS}
                            </label>
                            <Input
                                type="text"
                                id="command"
                                placeholder="Enter context"
                                value={context}
                                onChange={contextChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="command"
                                trigger="hover"
                                className="command-list auto"
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
                        <button className="auto-plan form-type left-btn" onClick={actionNew}>
                            Add
                        </button>
                        <button className="auto-plan form-type right-btn" onClick={closeNew}>
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
                <div className="Custom-modal auto-plan-confirm-modal command">
                    <p className="title">Edit</p>
                    <div className="content-with-title">
                        <FormGroup className={"command-input-modal" + (listType === Define.PLAN_TYPE_VFTP_COMPAT ? " hidden" : "")}>
                            <label>Data type</label>
                            <Input
                                type="text"
                                id="datatype"
                                placeholder="Enter data type"
                                value={dataType}
                                onChange={dataTypeChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="datatype"
                                trigger="hover"
                                className="command-list auto"
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
                            <label>
                                {listType === Define.PLAN_TYPE_VFTP_COMPAT ? inputTitleMsg.COMPAT : inputTitleMsg.SSS}
                            </label>
                            <Input
                                type="text"
                                id="command"
                                placeholder="Enter context"
                                value={context}
                                onChange={contextChanger}
                            />
                            <UncontrolledPopover
                                placement="right"
                                target="command"
                                trigger="hover"
                                className="command-list auto"
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
                        <button className="auto-plan form-type left-btn" onClick={actionEdit}>
                            Save
                        </button>
                        <button className="auto-plan form-type right-btn" onClick={closeEdit}>
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
                        <p>Do you want to delete command?</p>
                    </div>
                    <div className="button-wrap">
                        <button className="auto-plan form-type left-btn" onClick={actionDelete}>
                            Delete
                        </button>
                        <button className="auto-plan form-type right-btn" onClick={closeDelete}>
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
                        <button className="auto-plan alert-type" onClick={closeError}>
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

const CreateButtonArea = React.memo(({ ...props}) => {
    const {isOpen, isChecked, searchToggler, searchText, textChanger, itemToggler, openModal} = props;

    return (
        <div className="form-btn-section dis-flex">
            <div className={"search-btn-area" + (isOpen ? " active" : "")}>
                <ButtonToggle
                    outline
                    size="sm"
                    color="info"
                    className={"form-btn" + (isOpen ? " active" : "")}
                    onClick={searchToggler}
                >
                    <FontAwesomeIcon icon={faSearch}/>
                </ButtonToggle>
                <FormGroup>
                    <Input
                        type="text"
                        className="form-search-input"
                        placeholder="Enter the command to search."
                        value={searchText}
                        onChange={textChanger}
                    />
                </FormGroup>
            </div>
            <ButtonToggle
                outline
                size="sm"
                className={"form-btn toggle-all" + (isChecked ? " active" : "")}
                onClick={() => itemToggler(!isChecked)}
            >
                All
            </ButtonToggle>
            <Button outline size="sm" className="form-btn" onClick={openModal}>
                Add
            </Button>
        </div>
    );
}, propsCompare);

const CreateCommandList = React.memo(({ ...props }) => {
    const { commandList, checkHandler, query, editModal, deleteModal, type } = props;

    if (commandList.length === 0) {
        return (
            <FormGroup className="custom-scrollbar auto-plan-form-group pd-5 command-list targetlist">
                <div className="command-not-found">
                    <p><FontAwesomeIcon icon={faExclamationCircle} size="8x"/></p>
                    <p>No registered command.</p>
                </div>
            </FormGroup>
        );
    } else {
        const filteredList = createFilteredList(commandList, query);

        return (
            <FormGroup
                className={"custom-scrollbar auto-plan-form-group pd-5 command-list" + (filteredList.length > 0 ? "" : " targetlist")}>
                {filteredList.length > 0 ? (
                    <ul>
                        {filteredList.map((command, index) => {
                            let displayCommand = "";
                            if (type === Define.PLAN_TYPE_VFTP_COMPAT) {
                                displayCommand = command.cmd_name.replace("%s-%s-", "");
                            } else {
                                if (command.cmd_name.endsWith("%s")) {
                                    displayCommand = command.cmd_name.replace("-%s-%s", "");
                                } else {
                                    displayCommand = command.cmd_name.replace("-%s-%s-", "-");
                                }
                            }

                            return (
                                <li className="custom-control custom-checkbox" key={index}>
                                    <input
                                        type="checkbox"
                                        className="custom-control-input"
                                        id={command.id}
                                        value={command.cmd_name}
                                        checked={command.checked}
                                        onChange={checkHandler}
                                    />
                                    <label className="custom-control-label form-check-label" htmlFor={command.id}>
                                        {displayCommand}
                                    </label>
                                    {command.id !== -1 ? (
                                        <>
                                        <span className="icon" onClick={() => deleteModal(command.id)}>
                                            <FontAwesomeIcon icon={faTrash}/>
                                        </span>
                                            <span className="icon"
                                                  onClick={() => editModal(command.id, command.cmd_name)}>
                                            <FontAwesomeIcon icon={faPen}/>
                                        </span>
                                        </>
                                    ) : (<></>)}
                                </li>
                            );
                        })}
                    </ul>
                ) : (
                    <div className="command-not-found">
                        <p><FontAwesomeIcon icon={faExclamationCircle} size="8x"/></p>
                        <p>Command not found.</p>
                    </div>
                )}
            </FormGroup>
        );
    }
}, propsCompare);

const createFilteredList = (commandList, query) => {
    const regex = /(-)|(%s)/g;
    let createList = [];

    if (query.length > 0) {
        createList = commandList.filter(command => command.cmd_name.toLowerCase().replace(regex, "").includes(query.toLowerCase()));
    } else {
        createList = commandList.sort((a, b) => a.id - b.id);
    }

    return createList;
};

const isAllChecked = (filteredList) => {
    const allCnt = filteredList.length;
    let checkedCnt = 0;

    if (allCnt === 0) { return false; }

    filteredList.map(command => {
        if (command.checked) {
            checkedCnt++;
        }
    });

    return allCnt === checkedCnt;
}

export default connect(
    (state) => ({
        command: state.command.get('command'),
        autoPlan: state.autoPlan.get('autoPlan')
    }),
    (dispatch) => ({
        commandActions: bindActionCreators(commandActions, dispatch)
    })
)(RSSautoCommandList);