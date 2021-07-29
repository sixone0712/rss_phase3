import React, {useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
import * as API from "../../api";
import {
    vftpCompatSetRequestCommand,
    vftpCompatSetRequestEndDate,
    vftpCompatSetRequestStartDate
} from "../../modules/vftpCompat";
import {Breadcrumb, BreadcrumbItem, Col, Container, Row} from "reactstrap";
import ScrollToTop from "react-scroll-up";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faAngleDoubleUp} from "@fortawesome/free-solid-svg-icons";
import Machinelist from "../Manual/Machine/MachineList";
import Commandlist from "../Manual/VFTP/commandlist";
import Datesetting from "../Manual/VFTP/datesetting";
import Commandline from "../Manual/VFTP/commandline";
import Footer from "../Common/Footer";
import moment from "moment";
import * as Define from "../../define";
import services from "../../services";

const scrollStyle = {
    backgroundColor: "#343a40",
    width: "40px",
    height: "40px",
    textAlign: "center",
    borderRadius: "3px",
    zIndex: "101",
    bottom: "70px"
};

const modalMsgList ={
    cancel: "Are you sure you want to cancel the download?",
    process: "Downloading...",
    confirm: "Do you want to execute the command?",
    complete: "Download Complete!",
    ready:"",
}

function convertCommand (cmd, sDate,eDate) {
    let cmdString = '';
    const formatDate = 'YYYYMMDD_HHmmss';
    cmdString += (moment(sDate).format(formatDate) + '-'+moment(eDate).format(formatDate))
    cmdString += (cmd !== '') ? ('-' + cmd) :"";
    console.log("cmd: " + cmdString);
    return  cmdString + ".log";
}

const ManualVftpCompat = () => {
    const dbCommandList = useSelector(state => state.command.getIn(['command', "lists"]));
    const requestCommand = useSelector(state => state.vftpCompat.getIn(['requestList', "command"]));
    const fromDate = useSelector(state => state.vftpCompat.get('startDate'));
    const toDate = useSelector(state => state.vftpCompat.get('endDate'));
    const toolInfoList = useSelector(state => state.viewList.get('toolInfoList'));
    const dispatch = useDispatch();
    const commandList = API.vftpConvertDBCommand(dbCommandList.toJS());

    useEffect(() => {
        const setCommand = async () => {
            const selectCmd = commandList.find(item => item.checked && item.cmd_type === "vftp_compat");
            const convCmd = convertCommand(selectCmd === undefined ? "" : selectCmd.cmd_name, fromDate, toDate);
            await dispatch(vftpCompatSetRequestCommand(convCmd));
        }
        setCommand().then(r => r).catch(e => console.log(e));
    },[fromDate, toDate, commandList]);

    const checkRequest = () => {
        const newToolInfoList = toolInfoList.filter(item => item.get("checked") === true).toJS();
        const machineNames = newToolInfoList.map(item => item.targetname);
        if (machineNames.length === 0) return Define.SEARCH_FAIL_NO_MACHINE;
        if (fromDate.isAfter(toDate)) return Define.SEARCH_FAIL_DATE;
        return Define.RSS_SUCCESS;
    }

    const makeVFtpCompatDownloadRequestData = () => {
        const newToolInfoList = toolInfoList.filter(item => item.get("checked") === true).toJS();
        const fabNames = newToolInfoList.map(item => item.structId);
        const machineNames = newToolInfoList.map(item => item.targetname);

        return {
            "command": requestCommand,
            "fabNames": fabNames,
            "machineNames": machineNames
        }
    }

    const downloadFile = (downloadUrl) => {
         services.axiosAPI.downloadFile(downloadUrl).then(r => r);
    }

    return (
        <>
            <Container className="rss-container vftp manual" fluid={true}>
                <Breadcrumb className="topic-path">
                    <BreadcrumbItem>Manual Download</BreadcrumbItem>
                    <BreadcrumbItem active>VFTP (COMPAT)</BreadcrumbItem>
                </Breadcrumb>
                <Row>
                    <Col><Machinelist/></Col>
                    <Col><Commandlist cmdType={"vftp_compat"}/></Col>
                    <Col>
                        <Datesetting
                            from={fromDate}
                            FromDateChangehandler={(data) => dispatch(vftpCompatSetRequestStartDate(data))}
                            to={toDate}
                            ToDateChangehandler={(data) => dispatch(vftpCompatSetRequestEndDate(data))} />
                    </Col>
                </Row>
                <Row>
                    <Col>
                        <Commandline
                            type ="compat/optional"
                            string={requestCommand}
                            modalMsgList={modalMsgList}
                            checkRequest={checkRequest}
                            requestData={makeVFtpCompatDownloadRequestData}
                            saveData={downloadFile}
                        />
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

export default ManualVftpCompat;