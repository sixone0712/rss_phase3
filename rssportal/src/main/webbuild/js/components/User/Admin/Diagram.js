import React, {useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {
	faCircle,
	faDesktop,
	faExclamationCircle,
	faPen,
	faPlus,
	faProjectDiagram,
	faServer,
	faSyncAlt,
	faTrash
} from "@fortawesome/free-solid-svg-icons";
import {Card, CardBody, CardHeader, PopoverBody, PopoverHeader, UncontrolledPopover} from "reactstrap";
import DiagramModal from "./DiagramModal";
import AlertModal from "../../Common/AlertModal";
import {propsCompare} from "../../Common/CommonFunction";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../../modules/viewList";
import services from "../../../services";
import * as Define from "../../../define";

const msgList = {
	MSG_1: "This machine has being collected.",
	MSG_2: "MPA cannot be added because OTS has been deleted.",
	MSG_3: "MPA cannot be edited because OTS has been deleted.",
	MSG_4: "The name is already registered. Please register again."
};

const Diagram = React.memo(({toolInfoList, openDelete, toolInfoVer, viewListActions, loginInfo }) => {
	const [diagramModalOpen, setDiagramModalOpen] = useState(false);
	const [alertOpen, setAlertOpen] = useState(false);
	const [alertMsg, setAlertMsg] = useState("");
	const [sMachine, setSMachine] = useState(0);
	const data = toolInfoList.toJS();
	const otslist = data.filter(v => v["ots"] === null);
	const permission = loginInfo.get("auth");

	const updateMachineModal = async (flag, machine) => {
		if (flag === true) {
			const res = await isCollectingPlan(machine);
			console.log("res ",res);

			if (res === undefined) {
				setSMachine(machine);
				setDiagramModalOpen(flag);
			} else {
				openAlertModal(msgList.MSG_1);
			}
		} else {
			setSMachine(0);
			setDiagramModalOpen(flag);
		}
		console.log("machine :", machine);
	}

	const checkDeleteModal = async (flag, machine) => {
		const res = await isCollectingPlan(machine);
		console.log("[checkDeleteModal]res",res);
		if (res === undefined) {
			openDelete(flag,machine);
		} else {
			openAlertModal(msgList.MSG_1);
		}
	}

	const applyMachine = async (machine, edit,type) => {
		console.log("============Apply Machine=============");
		const res = await viewListActions.viewLoadToolInfoList(Define.REST_SYSTEM_GET_MACHINES);
		const {lists} = res.data;
		const v = lists.find(obj => obj.machineName === machine.targetname);
		const ots = type === "MPA"? lists.find(obj => obj.machineName === machine.ots) : null;
		let result = -1;
		let msg = "";
		console.log("====applyMachine====");
		console.log("machine",machine);
		console.log("edit",edit);
		console.log("type",type);

		if (edit) {
			const isCollecting = await isCollectingPlan(machine.targetname);

			if (isCollecting !== undefined) {
				result = 0;
				msg = msgList.MSG_1;
			} else if (type === "OTS"|| (type === "MPA" && ots !== undefined)) {
				result = 0;
				await services.axiosAPI.requestPatch(Define.REST_SYSTEM_SET_MACHINES, machine);
			} else {
				msg = msgList.MSG_3;
			}
		} else {
			if (v !== undefined) {
				msg = msgList.MSG_4;
			} else if (type === "MPA" && ots===undefined) {
				msg = msgList.MSG_2;
			} else {
				result = 0;
				await services.axiosAPI.requestPost(Define.REST_SYSTEM_SET_MACHINES, machine);
			}
		}

		if (result === 0) {
			await updateMachineModal(false, 0);
			await viewListActions.viewLoadToolInfoList(Define.REST_SYSTEM_GET_MACHINES);
		} else {
			setDiagramModalOpen(false);
			setTimeout(() => { openAlertModal(msg); }, 400);
		}

		console.log("==========================================");
		return result;
	}

	const isCollectingPlan = async (machine) => {
		const isOTS = otslist.find(v => v.targetname === machine);
		const res = await services.axiosAPI.requestGet(Define.REST_PLANS_GET_PLANS);
		const { lists } = res.data;
		if (isOTS !== undefined) {
			const mpa_list = data.filter(list => list.ots === machine);
			return mpa_list.length > 0
				? mpa_list.find(mpa => lists.find(plan => plan.machineNames.indexOf(mpa.targetname) !== -1 && plan.detailedStatus !== "completed"))
				: undefined;
		} else {
			return lists.find(plan => plan.machineNames.indexOf(machine) !== -1 && plan.detailedStatus !== "completed")
		}
	}

	const openAlertModal = (msg) => {
		setAlertMsg(msg);
		setAlertOpen(true);
	}

	const closeAlertModal = async () => {
		const res = await viewListActions.viewLoadToolInfoList(Define.REST_SYSTEM_GET_MACHINES);

		setAlertOpen(false);
		setAlertMsg("");

		if (alertMsg === msgList.MSG_4 || (alertMsg === msgList.MSG_2 && res.data.lists.length !== 0)) {
			setTimeout(() => { setDiagramModalOpen(true); }, 400);
		}
	};

	return (
		<>
			<DiagramModal
				isOpen={diagramModalOpen}
				close={updateMachineModal}
				apply={(data,selectIndex, type) => applyMachine(data, selectIndex, type)}
				data={data.find((item) => { return item.targetname === sMachine })}
				otsList={otslist}
				eMachine={sMachine}
			/>
			<AlertModal
				isOpen={alertOpen}
				icon={faExclamationCircle}
				message={alertMsg}
				style="system-setting"
				closer={closeAlertModal}
			/>
			<Card className="admin-system diagram">
				<CardHeader>
					<p>
						<span className="title-icon"><FontAwesomeIcon icon={faProjectDiagram}/></span>
						System Diagram<span className="title-version">(Version: {toolInfoVer})</span>
					</p>
					{ permission.config &&
						<div className={"button-area"}>
							<div className="refresh-btn">
								<button onClick={() => viewListActions.viewLoadToolInfoList(Define.REST_SYSTEM_GET_MACHINES)}>
									<div className="bg" />
									<div className="text"><FontAwesomeIcon icon={faSyncAlt} /></div>
								</button>
							</div>
							<div className="add-btn">
								<button onClick={() => updateMachineModal(true, 0)}>
									<div className="bg" />
									<div className="text"><FontAwesomeIcon icon={faPlus} /></div>
								</button>
							</div>
						</div>
					}
				</CardHeader>
				<CardBody>
					{ permission.config
						?
							<MachineList data={data} openEdit={updateMachineModal} openDelete={checkDeleteModal}	openAlert={openAlertModal} />
						:
							<div className={"diagram-no-permission"}>
								<p><FontAwesomeIcon icon={faExclamationCircle} size={"8x"}/></p>
								<p className={"diagram-no-permission-text"}>You don't have permission.</p>
							</div>
					}
				</CardBody>
			</Card>
		</>
	);
});

const MachineList = React.memo(({data, openEdit, openDelete, openAlert}) => {
	const ots_list = data.filter(v => v["ots"] === null);

	return (
		<div className="diagram">
			<ul>
				<li>
					<span className="item-name"><FontAwesomeIcon icon={faServer}/>{" "}ESP</span>
					{ots_list.length > 0 ? (
						<ul>
							{ots_list.map((ots, idx) => {
								const spanId = "ots_" + idx;
								const mpa_list = data.filter(item => {return ots.targetname === item.ots});
								const status = ots.ftpConnected ? 'machine-status-running' : "machine-status-unknown";
								return (
									<li key={idx}>
										<div id={spanId} className="item-name">
											<FontAwesomeIcon icon={faDesktop}/>
											<FontAwesomeIcon className={status} icon={faCircle}/>
											{" "}{ots.targetname}</div>
										<UncontrolledPopover
											placement="left"
											target={spanId}
											trigger="hover"
											hideArrow={true}
											fade={false}
											className="admin-system"
										>
											<PopoverHeader>
												<span>{ots.targetname}</span>
												<div className="action">
													<span onClick={() => openEdit(true, ots.targetname)}><FontAwesomeIcon icon={faPen}/></span>
													<span
														onClick={() => {
															mpa_list.length > 0
																? openAlert("OTS cannot be deleted because there is a connected MPA.")
																: openDelete("MACHINE", ots.targetname);
														}}>
														<FontAwesomeIcon icon={faTrash}/>
													</span>
												</div>
											</PopoverHeader>
											<PopoverBody>
												<p>Status: {ots.ftpConnected ? "Connected" : "Disconnected"}</p>
												<p>IP Address: {ots.host}</p>
												<p>Port: {ots.port}</p>
											</PopoverBody>
										</UncontrolledPopover>
										{mpa_list.length > 0 ? (
											<ul>
												{mpa_list.map((item, index) => {
													let status = 'machine-status-warning';
													if(item.ftpConnected && item.vftpConnected) {
														status = 'machine-status-running'
													} else if(!item.ftpConnected && !item.vftpConnected) {
														status = 'machine-status-unknown'
													}
													const spanId = "mpa_" + idx + index;
													return (
														<li key={index}>
															<div id={spanId} className="item-name">
																<FontAwesomeIcon icon={faDesktop}/>
																<FontAwesomeIcon className={status} icon={faCircle} />
																{" "}{item.targetname}
															</div>
															<UncontrolledPopover
																placement="right"
																target={spanId}
																trigger="hover"
																hideArrow={true}
																fade={false}
																className="admin-system"
															>
																<PopoverHeader>
																	<span>{item.targetname}</span>
																	<div className="action">
																		<span onClick={() => openEdit(true, item.targetname)}><FontAwesomeIcon icon={faPen}/></span>
																		<span onClick={() => openDelete("MACHINE", item.targetname)}><FontAwesomeIcon icon={faTrash}/></span>
																	</div>
																</PopoverHeader>
																<PopoverBody>
																	<p>FTP Status: {item.ftpConnected ? "Connected" : "Disconnected"}</p>
																	<p>VFTP Status: {item.vftpConnected ? "Connected" : "Disconnected"}</p>
																	<p>IP Address: {item.host}</p>
																	<p>Line: {item.line}</p>
																	<p>Tool Type: {item.toolType}</p>
																	<p>Serial Number: {item.serialNumber}</p>
																	<p>FTP Login User: {item.ftpUser}</p>
																	<p>FTP Login Password: {item.ftpPassword ? "*******" : ""}</p>
																	<p>VFTP Login User: {item.vftpUser}</p>
																	<p>VFTP Login Password: {item.vftpPassword ? "*******" : ""}</p>
																</PopoverBody>
															</UncontrolledPopover>
														</li>
													);
												})}
											</ul>
										) : (
											""
										)}
									</li>
								);
							})}
						</ul>
					) : ( "" )}
				</li>
			</ul>
		</div>
	);
}, propsCompare);

export default connect(
	(state) => ({
		toolInfoList: state.viewList.get('toolInfoList'),
		toolInfoVer: state.viewList.get('toolInfoVer'),
		loginInfo : state.login.get('loginInfo'),
	}),
	(dispatch) => ({
		viewListActions: bindActionCreators(viewListActions, dispatch),
	}),
)(Diagram);