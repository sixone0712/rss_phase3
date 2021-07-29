import React, {useEffect, useState} from "react";
import {Row, Col, Card, CardHeader, CardBody, FormGroup, Container} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
	faFile,
	faUsers,
	faCloudUploadAlt,
	faAngleDoubleUp,
	faTrash,
	faExclamationCircle,
	faTools
} from "@fortawesome/free-solid-svg-icons";
import {Link as RRLink} from "react-router-dom";
import ScrollToTop from "react-scroll-up";
import * as Define from "../../../define";
import Footer from "../../Common/Footer";
import Diagram from "./Diagram";
import ImportModal from "./ImportModal";
import ConfirmModal from "../../Common/ConfirmModal";
import CategoryList from "./CategoryList";
import AlertModal from "../../Common/AlertModal";
import {connect} from 'react-redux'
import services from "../../../services";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../../modules/viewList";
import ExportExcel from "./ExportExcel";

const alertMsg = {
	DELETE_DIAGRAM: "Do you want to delete OTS(or MPA)?",
	DELETE_CATEGORY: "Do you want to delete category?"
};

const scrollStyle = {
	backgroundColor: "#343a40",
	width: "40px",
	height: "40px",
	textAlign: "center",
	borderRadius: "3px",
	zIndex: "101",
	bottom: "70px"
};

const SystemSettings = ({viewListActions, loginInfo}) => {
	const [importOpen, setImportOpen] = useState(false);
	const [deleteOpen, setDeleteOpen] = useState(false);
	const [alertOpen, setAlertOpen] = useState(false);
	const [modalMsg, setModalMsg] = useState("");
	const [modalIcon, setModalIcon] = useState("");
	const [totalUserCnt, SetTotalUserCnt] = useState(0);
	const [dlHistoryCnt, SetDlHistoryCnt] = useState(0);
	const [sValue, SetValue] = useState(0);
	const permission = loginInfo.get("auth");

	const openDeleteModal = (msg, idx) => {
		msg === "CATEGORY" ? setModalMsg(alertMsg.DELETE_CATEGORY) : setModalMsg(alertMsg.DELETE_DIAGRAM);
		updateSelectValue(true,idx);
		setDeleteOpen(true);
	};

	const openAlertModal = (msg, icon) => {
		setModalMsg(msg);
		setModalIcon(icon);
		setAlertOpen(true);
	};

	const closeDeleteModal = async (flag) => {
		if(flag === "DELETE"){
			if ((modalMsg === alertMsg.DELETE_CATEGORY)) {
				await services.axiosAPI.requestDelete(Define.REST_SYSTEM_SET_CATEGORIES + "/" + sValue).then(r => r).catch((e) => {console.log(e)});
				await viewListActions.viewLoadLogTypeList(Define.REST_SYSTEM_GET_CATEGORIES);
			} else {
				await services.axiosAPI.requestDelete(Define.REST_SYSTEM_SET_MACHINES + "/" + sValue).then(r => r).catch((e) => {console.log(e)});
				await viewListActions.viewLoadToolInfoList(Define.REST_SYSTEM_GET_MACHINES);
			}
		}
		setModalMsg("");
		updateSelectValue(false, 0);
		setDeleteOpen(false);
	};

	const getTotalUserCnt = async () => {
		console.log("============getTotalUserCnt=============");
		const res = await services.axiosAPI.requestGet(Define.REST_USERS_GET_TOTAL_CNT);
		const {cnt: UserCnt} = res.data;
		SetTotalUserCnt(UserCnt);
	}

	const getDlHistoryCnt = async () => {
		console.log("============getTotalHistoryCnt=============");
		const res = await services.axiosAPI.requestGet(Define.REST_HISTORIES_GET_TOTAL_CNT);
		const {cnt: HistoryCnt} = res.data;
		SetDlHistoryCnt(HistoryCnt);
	}

	const updateSelectValue = (flag, index) => {
		flag === true ? SetValue(index) : SetValue(0);
	}

	const ImportProcess = async () => {
		const res = await services.axiosAPI.requestGet(Define.REST_PLANS_GET_PLANS);
		const {lists} = res.data;
		const isCollectingPlan = lists.find(plan => plan.detailedStatus !== "completed")
		if (isCollectingPlan === undefined || isCollectingPlan == null) setImportOpen(true);
		else openAlertModal("Import is not possible because there is job being collected.", faExclamationCircle);
	}

	useEffect(()=>{
		const init = async () => {
			await getDlHistoryCnt();
			await getTotalUserCnt();
		}
		init().then(r => r).catch(e => console.log(e));
	},[]);

	return (
		<>
			<ConfirmModal
				isOpen={deleteOpen}
				icon={faTrash}
				message={modalMsg}
				leftBtn="Yes"
				rightBtn="No"
				style="system-setting"
				actionBg={() => closeDeleteModal("CLOSE")}
				actionLeft={() => closeDeleteModal("DELETE")}
				actionRight={() => closeDeleteModal("CLOSE")}
			/>
			<AlertModal
				isOpen={alertOpen}
				icon={modalIcon}
				message={modalMsg}
				style="system-setting"
				closer={() => setAlertOpen(false)}
			/>
			<ImportModal isOpen={importOpen} close={() => setImportOpen(false)} alertOpen={openAlertModal} />
			<Container className="rss-container" fluid={true}>
				<Row className="pd-0 mt-2">
					<Col>
						<Diagram openDelete={openDeleteModal} />
					</Col>
					<Col xs="4" className="small-card-grid">
						<div className="grid-body">
							<Card className="admin-system small">
								<CardHeader>Collected Files</CardHeader>
								<CardBody className="info">
									{ permission.account
										?
										<p className="center-text">
											<RRLink to={Define.PAGE_REFRESH_ADMIN_DL_HISTORY}>
												<FontAwesomeIcon icon={faFile} />{" "}{dlHistoryCnt}
											</RRLink>
										</p>
										:
										<div className={"setting-no-permission"}>
											<p><FontAwesomeIcon icon={faExclamationCircle} size={"5x"}/></p>
											<p>You don't have permission.</p>
										</div>
									}
								</CardBody>
							</Card>
							<Card className="admin-system small">
								<CardHeader>Registered Users</CardHeader>
								<CardBody className="info">
									{ permission.account
										?
										<p className="center-text">
											<RRLink to={Define.PAGE_REFRESH_ADMIN_ACCOUNT}>
												<FontAwesomeIcon icon={faUsers}/>{" "}{totalUserCnt}
											</RRLink>
										</p>
										:
										<div className={"setting-no-permission"}>
											<p><FontAwesomeIcon icon={faExclamationCircle} size={"5x"}/></p>
											<p>You don't have permission.</p>
										</div>
									}
								</CardBody>
							</Card>
							<Card className="admin-system small">
								<CardHeader>System Configuration</CardHeader>
								<CardBody className="control import">
									{ permission.config
										?
										<>
											<FormGroup className="control">
												<button className="control-btn" onClick={() => ImportProcess()}>
													<span><FontAwesomeIcon icon={faCloudUploadAlt} size="lg"/></span>
													<span>import</span>
												</button>
											</FormGroup>
											<FormGroup className="control">
												<ExportExcel/>
											</FormGroup>
										</>
										:
										<div className={"setting-no-permission"}>
											<p><FontAwesomeIcon icon={faExclamationCircle} size={"5x"}/></p>
											<p>You don't have permission.</p>
										</div>
									}
								</CardBody>
							</Card>
							<Card className="admin-system small">
								<CardHeader>Service Manager</CardHeader>
								<CardBody className="control">
									{ (permission.system_log || permission.system_restart)
										?
										<FormGroup className="control">
											<button className="control-btn"
													onClick={() => window.open(`http://${window.location.host}/servicemanager`)}>
												<span><FontAwesomeIcon icon={faTools} size="lg"/></span>
												<span>open</span>
											</button>
										</FormGroup>
										:
										<div className={"setting-no-permission"}>
											<p><FontAwesomeIcon icon={faExclamationCircle} size={"5x"}/></p>
											<p>You don't have permission.</p>
										</div>
									}
								</CardBody>
							</Card>
						</div>
					</Col>
				</Row>
				<CategoryList openDelete={openDeleteModal} />
			</Container>
			<Footer/>
			<ScrollToTop showUnder={160} style={scrollStyle}>
				<span className="scroll-up-icon"><FontAwesomeIcon icon={faAngleDoubleUp} size="lg"/></span>
			</ScrollToTop>
		</>
	);
};

export default connect(
	(state) => ({
		toolInfoList: state.viewList.get('toolInfoList'),
		logInfoList: state.viewList.get('logInfoList'),
		loginInfo : state.login.get('loginInfo'),
	}),
	(dispatch) => ({
		viewListActions: bindActionCreators(viewListActions, dispatch),
	}),
)(SystemSettings);