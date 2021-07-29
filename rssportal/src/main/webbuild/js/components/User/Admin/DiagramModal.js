import React, {useState, useRef} from "react";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {FormGroup, Input} from "reactstrap";
import {Select} from "antd";
import {propsCompare, stringBytes} from "../../Common/CommonFunction";
import ModalTooltip from "./ModalTooltip";

const { Option } = Select;

const notNullList = [
	"mpa_name",
	"mpa_ip",
	"mpa_line",
	"ots_name",
	"ots_ip",
	"ots_port"
];

const DiagramModal = React.memo(({isOpen, apply, close, data, otsList, eMachine}) => {
	console.log("data",data);
	const [selectMachine, setSelectMachine] = useState("");
	const [mpaInputError, setMpaInputError] = useState([]);
	const [otsInputError, setOtsInputError] = useState([]);
	const [initLoader, setInitLoader] = useState(false);
	const [mpaInfo, setMpaInfo] = useState(
		{
			host: '',
			line: '',
			ots: '',
			ftpPassword: '',
			ftpUser: '',
			vftpPassword: '',
			vftpUser: '',
			serialNumber: '',
			structId: '',
			targetname: '',
			toolType: ''
		});
	const [otsInfo, setOtsInfo] = useState(
		{
			host: '',
			targetname: '',
			port:'',
		});

	const mpaSection = useRef();
	const otsSection = useRef();

	if(initLoader === false && isOpen ) {
		setMpaInfo({
			host : (data && (data.ots))?data.host:'',
			line : (data && (data.ots))?data.line:'',
			ots : (data && (data.ots))?data.ots:'',
			ftpPassword : (data && (data.ots))?data.ftpPassword:'',
			ftpUser : (data && (data.ots))?data.ftpUser:'',
			vftpPassword: (data && (data.ots))?data.vftpPassword:'',
			vftpUser: (data && (data.ots))?data.vftpUser:'',
			serialNumber : (data && (data.ots))?data.serialNumber:'',
			structId: (data && (data.ots))?data.structId:'',
			targetname: (data && (data.ots))?data.targetname:'',
			toolType: (data && (data.ots))?data.toolType:''
		});
		setOtsInfo({
			host : (data && !(data.ots))?data.host:'',
			targetname: (data && !(data.ots))?data.targetname:'',
			port: (data && !(data.ots))?data.port:''
		});
		setInitLoader(true);
		eMachine === 0 || (eMachine && data.ots) ? setSelectMachine("MPA") : setSelectMachine("OTS");
	}

	const closeDisplay = () => {
		close(false, 0);
		setInitLoader(false);
		setMpaInputError([]);
		setOtsInputError([]);
	};

	const applyFunc = async (data, editMachine) => {
		const errorData = selectMachine === "MPA" ? mpaInputError : otsInputError;
		if (errorData.length === 0) {
			const sectionData = selectMachine === "MPA" ? mpaSection : otsSection;
			const { children } = sectionData.current;
			let idx = selectMachine === "MPA" ? 1 : 0;
			const maxLen = selectMachine === "MPA" ? 3 : 2;
			const idList = [];

			for (idx; idx <= maxLen; idx++) {
				const { id, value } = children[idx].children[1];
				if (value === "" && notNullList.indexOf(id) !== -1) {
					idList.push(id);
				}
			}

			if (selectMachine === "MPA" && mpaInfo.ots === "") {
				idList.push("mpa_ots");
			}

			if (idList.length === 0) {
				console.log("selectMachine", selectMachine);
				if(await apply(data, editMachine, selectMachine) === 0){
					setInitLoader(false);
					setMpaInputError([]);
					setOtsInputError([]);
				}
			} else {
				selectMachine === "MPA" ? setMpaInputError(mpaInputError.concat(idList)) : setOtsInputError(otsInputError.concat(idList));
			}
		}
	};

	const selectChange = (value) => {
		setMpaInfo({...mpaInfo, ots: value});
		setMpaInputError(value === "" ? [...mpaInputError, "mpa_ots"] : mpaInputError.filter(item => item !== "mpa_ots"));
	};

	const changeHandler = (e) => {
		const { id, name, value, maxLength } = e.target;
		selectMachine === 'MPA' ? setMpaInfo({...mpaInfo, [name]: value})	: setOtsInfo({...otsInfo, [name]: value});
		selectMachine === 'MPA'
			? setMpaInputError(!inputCheck(value, maxLength, id) ? [...mpaInputError, id] : mpaInputError.filter(item => item !== id))
			: setOtsInputError(!inputCheck(value, maxLength, id) ? [...otsInputError, id] : otsInputError.filter(item => item !== id));
	};

	const inputCheck = (value, maxLen, id) => {
		if (notNullList.indexOf(id) === -1) {
			return stringBytes(value) <= maxLen
		} else {
			return stringBytes(value) <= maxLen && value !== "";
		}
	}

	console.log("=================DiagramModal================");
	console.log("eMachine", eMachine);
	console.log("mpaInfo", mpaInfo);
	console.log("otsInfo", otsInfo);
	console.log("=================DiagramModal================");

	return (
		<>
			{isOpen ? (
				<ReactTransitionGroup
					transitionName={"Custom-modal-anim"}
					transitionEnterTimeout={200}
					transitionLeaveTimeout={200}
				>
					<div className="Custom-modal-overlay" onClick={closeDisplay} />
					<div className="Custom-modal">
						<p className="title">Machine Add/Edit</p>
						<div className="content-with-title system-setting">
							<div className="machine-tabs">
								<input
									type="radio"
									id="mpa"
									name="tab-control"
									defaultChecked={selectMachine === "MPA"}
									disabled={eMachine !== 0 && otsInfo.targetname}
									onClick={() => setSelectMachine("MPA")} />
								<input
									type="radio"
									id="ots"
									name="tab-control"
									defaultChecked={selectMachine === "OTS"}
									disabled={eMachine !== 0 && mpaInfo.targetname}
									onClick={() => setSelectMachine("OTS")} />
								<ul>
									<li><label htmlFor="mpa">MPA</label></li>
									<li><label htmlFor="ots">OTS</label></li>
								</ul>
								<div className="machine-tabs-slider">
									<div className="indicator" />
								</div>
								<div className="machine-tabs-content">
									<section ref={mpaSection}>
										<FormGroup>
											<label className="title">OTS</label>
											<Select
												defaultValue={mpaInfo.ots ? mpaInfo.ots : ""}
												style={{width: "100%", borderRadius: ".25rem"}}
												onChange={(v) => selectChange(v)}
												className="diagram-grid"
											>
												{otsList.length > 0 && (otsList.map((item, key) => {
													return <Option className="admin-system" value={item.targetname} key={key}>{item.targetname}</Option>;
												}))}
											</Select>
											<span className={"error" + (mpaInputError.indexOf("mpa_ots") !== -1 ? " active" : "")}>OTS is not selected.</span>
										</FormGroup>
										<FormGroup>
											<label className="title">Name</label>
											<Input
												type="text"
												id="mpa_name"
												name="targetname"
												value={mpaInfo.targetname}
												onChange={(e)=>changeHandler(e)}
												disabled={eMachine !== 0}
												maxLength="25" />
											<span className={"error" + (mpaInputError.indexOf("mpa_name") !== -1 ? " active" : "")}>Name is invalid.</span>
											<ModalTooltip	target="mpa_name"	header="Name"	body="You can input up to 25 byte."	placement="bottom" trigger="focus"/>
										</FormGroup>
										<FormGroup>
											<label className="title">IP Address</label>
											<Input type="text" id="mpa_ip" name="host" value={mpaInfo.host} onChange={(e)=>changeHandler(e)}  maxLength="50" />
											<span className={"error" + (mpaInputError.indexOf("mpa_ip") !== -1 ? " active" : "")}>IP Address is invalid.</span>
											<ModalTooltip target="mpa_ip" header="IP Address" body="You can input up to 50 byte." placement="bottom" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">Line</label>
											<Input type="text" id="mpa_line" name="line" value={mpaInfo.line} onChange={(e)=>changeHandler(e)} maxLength="25" />
											<span className={"error" + (mpaInputError.indexOf("mpa_line") !== -1 ? " active" : "")}>Line is invalid.</span>
											<ModalTooltip target="mpa_line" header="Line" body="You can input up to 25 byte." placement="bottom" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">Tool Type</label>
											<Input type="text" id="mpa_ttype" name="toolType" value={mpaInfo.toolType} onChange={(e)=>changeHandler(e)} maxLength="25"/>
											<span className={"error" + (mpaInputError.indexOf("mpa_ttype") !== -1 ? " active" : "")}>Tool Type is invalid.</span>
											<ModalTooltip target="mpa_ttype" header="Tool Type" body="You can input up to 25 byte." placement="bottom" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">Serial Number</label>
											<Input
												type="text"
												id="mpa_serial"
												name="serialNumber"
												value={mpaInfo.serialNumber}
												onChange={(e)=>changeHandler(e)}
												maxLength="25" />
											<span className={"error" + (mpaInputError.indexOf("mpa_serial") !== -1 ? " active" : "")}>Serial Number is invalid.</span>
											<ModalTooltip target="mpa_serial" header="Serial Number" body="You can input up to 25 byte." placement="bottom" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">FTP Login User</label>
											<Input type="text" id="mpa_ftpuser" name="ftpUser" value={mpaInfo.ftpUser} onChange={(e)=>changeHandler(e)} maxLength="20" />
											<span className={"error" + (mpaInputError.indexOf("mpa_ftpuser") !== -1 ? " active" : "")}>FTP Login User is invalid.</span>
											<ModalTooltip target="mpa_ftpuser" header="FTP Login User" body="You can input up to 20 byte." placement="bottom" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">FTP Login Password</label>
											<Input
												type="password"
												id="mpa_ftppw"
												name="ftpPassword"
												value={mpaInfo.ftpPassword}
												onChange={(e)=>changeHandler(e)}
												maxLength="20" />
											<span className={"error" + (mpaInputError.indexOf("mpa_ftppw") !== -1 ? " active" : "")}>FTP Login Password is invalid.</span>
											<ModalTooltip target="mpa_ftppw" header="FTP Login Password" body="You can input up to 20 byte." placement="bottom" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">VFTP Login User</label>
											<Input
												type="text"
												id="mpa_vftpuser"
												name="vftpUser"
												value={mpaInfo.vftpUser}
												onChange={(e)=>changeHandler(e)}
												maxLength="20" />
											<span className={"error" + (mpaInputError.indexOf("mpa_vftpuser") !== -1 ? " active" : "")}>VFTP Login User is invalid.</span>
											<ModalTooltip target="mpa_vftpuser" header="FTP Login User" body="You can input up to 20 byte." placement="bottom" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">VFTP Login Password</label>
											<Input
												type="password"
												id="mpa_vftppw"
												name="vftpPassword"
												value={mpaInfo.vftpPassword}
												onChange={(e)=>changeHandler(e)}
												maxLength="20" />
											<span className={"error" + (mpaInputError.indexOf("mpa_vftppw") !== -1 ? " active" : "")}>VFTP Login Password is invalid.</span>
											<ModalTooltip target="mpa_vftppw" header="FTP Login Password" body="You can input up to 20 byte." placement="bottom" trigger="focus" />
										</FormGroup>
									</section>
									<section ref={otsSection}>
										<FormGroup>
											<label className="title">Name</label>
											<Input
												type="text"
												id="ots_name"
												name="targetname"
												value={otsInfo.targetname}
												onChange={(e)=>changeHandler(e)}
												disabled={eMachine !== 0}
												maxLength="25" />
											<span className={"error" + (otsInputError.indexOf("ots_name") !== -1 ? " active" : "")}>Name is invalid.</span>
											<ModalTooltip target="ots_name" header="Name" body="You can input up to 25 byte." placement="right" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">IP Address</label>
											<Input type="text" id="ots_ip" name="host" value={otsInfo.host} onChange={(e)=>changeHandler(e)} maxLength="50" />
											<span className={"error" + (otsInputError.indexOf("ots_ip") !== -1 ? " active" : "")}>IP Address is invalid.</span>
											<ModalTooltip target="ots_ip" header="IP Address" body="You can input up to 50 byte." placement="right" trigger="focus" />
										</FormGroup>
										<FormGroup>
											<label className="title">Port</label>
											<Input type="text" id="ots_port" name="port" value={otsInfo.port} onChange={(e)=>changeHandler(e)} maxLength="6"/>
											<span className={"error" + (otsInputError.indexOf("ots_port") !== -1 ? " active" : "")}>Port is invalid.</span>
											<ModalTooltip target="ots_port" header="Port" body="You can input up to 6 byte." placement="right" trigger="focus" />
										</FormGroup>
									</section>
								</div>
							</div>
						</div>
						<div className="button-wrap">
							<button
								className="form-type left-btn system-setting"
								onClick={()=> applyFunc(selectMachine === "MPA" ? mpaInfo : otsInfo,eMachine)}>
								Apply
							</button>
							<button className="form-type right-btn system-setting" onClick={closeDisplay}>
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
}, propsCompare);

export default DiagramModal;