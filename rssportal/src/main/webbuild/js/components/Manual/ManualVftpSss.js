import React, {useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
import {
    vftpSssInitResponseList,
    vftpSssSetRequestCommand,
    vftpSssSetRequestEndDate,
    vftpSssSetRequestStartDate,
    vftpSssSetResponseList,
} from "../../modules/vftpSss";
import {Breadcrumb, BreadcrumbItem, Col, Container, Row} from "reactstrap";
import ScrollToTop from "react-scroll-up";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faAngleDoubleUp} from "@fortawesome/free-solid-svg-icons";
import Machinelist from "../Manual/Machine/MachineList";
import Datesetting from "../Manual/VFTP/datesetting";
import Commandlist from "../Manual/VFTP/commandlist";
import Commandline from "../Manual/VFTP/commandline";
import Filelist from "./VFTP/filelist";
import Footer from "../Common/Footer";
import * as API from "../../api";
import moment from "moment";
import * as Define from "../../define";

const scrollStyle = {
	backgroundColor: "#343a40",
	width: "40px",
	height: "40px",
	textAlign: "center",
	borderRadius: "3px",
	zIndex: "101",
	bottom: "70px"
};

const modalMsglist ={
    cancel: "Are you sure you want to cancel the search?",
    process: "Searching.....",
    confirm: "Do you want to execute the command?",
    complete: "Search was canceled.",
    ready:"",
}

function convertCommand (cmd, sDate, eDate) {
    const splitCmd = cmd.split('-');
    const command = splitCmd[0];
    const option = splitCmd.length > 1 ? `-${splitCmd[1]}` : "";
    const formatDate = 'YYYYMMDD_HHmmss';
    return `${command}-${moment(sDate).format(formatDate)}-${moment(eDate).format(formatDate)}${option}`;
}

const ManualVftpSss = () => {
    const dbCommandList = useSelector(state => state.command.getIn(['command', "lists"]));
    const requestCommand = useSelector(state => state.vftpSss.getIn(['requestList', "command"]));
    const fromDate = useSelector(state => state.vftpSss.get('startDate'));
    const toDate = useSelector(state => state.vftpSss.get('endDate'));
    const toolInfoList = useSelector(state => state.viewList.get('toolInfoList'));
    const dispatch = useDispatch();
    const commandList = API.vftpConvertDBCommand(dbCommandList.toJS());

    useEffect(() => {
        const setCommand = async () => {
            const selectCmd = commandList.find(item => item.checked && item.cmd_type === "vftp_sss");
            const convCmd = convertCommand(selectCmd === undefined ? "" : selectCmd.cmd_name, fromDate, toDate);
            await dispatch(vftpSssSetRequestCommand(convCmd));
        }
        setCommand().then(r => r).catch(e => console.log(e));
    },[fromDate, toDate, commandList]);

    const checkRequest = () => {
        const newToolInfoList = toolInfoList.filter(item => item.get("checked") === true).toJS();
        const machineNames = newToolInfoList.map(item => item.targetname);
        const checkCmd = commandList.filter(item => item.checked === true);
        if (machineNames.length === 0) return Define.SEARCH_FAIL_NO_MACHINE;
        if (checkCmd.length === 0) return Define.SEARCH_FAIL_NO_COMMAND;
        if (fromDate.isAfter(toDate)) return Define.SEARCH_FAIL_DATE;

        dispatch(vftpSssInitResponseList());
        return Define.RSS_SUCCESS;
    }

    const makeVFtpSSSSearchRequestData = () => {
        const newToolInfoList = toolInfoList.filter(item => item.get("checked") === true).toJS();
        const fabNames = newToolInfoList.map(item => item.structId);
        const machineNames = newToolInfoList.map(item => item.targetname);

        return {
            "command": requestCommand,
            "fabNames": fabNames,
            "machineNames": machineNames
        }
    }

    const saveResponseData = (data) => {
        dispatch(vftpSssSetResponseList(data))
    }

    return (
        <>
            <Container className="rss-container vftp manual" fluid={true}>
                <Breadcrumb className="topic-path">
                    <BreadcrumbItem>Manual Download</BreadcrumbItem>
                    <BreadcrumbItem active>VFTP (SSS)</BreadcrumbItem>
                </Breadcrumb>
                <Row>
                    <Col><Machinelist/></Col>
                    <Col><Commandlist cmdType={"vftp_sss"} /></Col>
                    <Col>
                        <Datesetting
                            from={fromDate}
                            FromDateChangehandler={(data) => dispatch(vftpSssSetRequestStartDate(data))}
                            to={toDate}
                            ToDateChangehandler={(data) => dispatch(vftpSssSetRequestEndDate(data))}
                        />
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <Commandline
                            type ="sss/optional"
                            string={requestCommand}
                            modalMsgList={modalMsglist}
                            checkRequest={checkRequest}
                            requestData={makeVFtpSSSSearchRequestData}
                            saveData={saveResponseData}
                        />
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <Filelist />
                    </Col>
                </Row>
            </Container>
            <Footer/>
            <ScrollToTop showUnder={160} style={scrollStyle}>
                <span className="scroll-up-icon"><FontAwesomeIcon icon={faAngleDoubleUp} size="lg"/></span>
            </ScrollToTop>
        </>
    );
};

export default ManualVftpSss;