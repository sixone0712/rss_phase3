import React, {useState, useRef} from "react";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {FormGroup, Input} from "reactstrap";
import {Select} from "antd";
import {propsCompare, stringBytes} from "../../Common/CommonFunction";
import ModalTooltip from "./ModalTooltip";

const { Option } = Select;

const notNullList = [
	"logNo",
	"logName",
	"filePath",
	"fileName"
];

const CategoryModal = React.memo(({isOpen, apply, close, data, isEdit}) => {
	const [initLoader, setInitLoader] = useState(false);
	const [inputError, setInputError] = useState([]);
	const [CategoryInfo, setCategoryInfo] = useState(
		{
			logName :'',
			logNo : '',
			logCode : '',
			filePath : '',
			description : '',
			fileName : '',
			display: false,
			auto: false,
			dest:'Cons'
		});
	const selectCategory = (isEdit) ? data.find((item) => { return item.logCode === isEdit }) : 0;
	const allInput = useRef();

	if(isOpen && (initLoader === false)) {
		setCategoryInfo({
			logName : (selectCategory) ? selectCategory.logName : '',
			logNo : (selectCategory) ? parseInt(selectCategory.logCode,16): '',
			logCode : (selectCategory) ? selectCategory.logCode : '',
			filePath : (selectCategory) ? selectCategory.filePath : '',
			description : (selectCategory) ? selectCategory.description : '',
			fileName : (selectCategory) ? selectCategory.fileName : '',
			display: (selectCategory) ? selectCategory.display : false,
			auto: (selectCategory) ? selectCategory.auto : false,
			dest: (selectCategory) ? selectCategory.dest : 'Cons'
		});
		setInitLoader(true);
	}

	const pad = (n, width)=> {
		n = n + '';
		return n.length >= width ? n : new Array(width - n.length + 1).join('0') + n;
	}

	const changeHandler = (e) => {
		const {name, value, maxLength} = e.target;
		if (name === "logNo") {
			const HexCode = pad(Number(value).toString(16).toUpperCase(),3);
			setCategoryInfo(inputs => ({...inputs, logCode: HexCode}));
		}
		setCategoryInfo(inputs => ({...inputs, [name]: value}));
		setInputError(!inputCheck(value, maxLength, name) ? [...inputError, name] : inputError.filter(item => item !== name));
	};

	const changeToggleHandler = (e) => {
		const { name, checked } = e.target;
		setCategoryInfo({...CategoryInfo, [name]: checked});
	};

	const applyFunc = async (data, editCode) => {
		if (inputError.length === 0) {
			const { children } = allInput.current;
			const nameList = [];

			for (let i = 0; i <= 3; i++) {
				const { name, value } = children[i].children[1];
				if (value === "" && notNullList.indexOf(name) !== -1) {
					nameList.push(name);
				}
			}

			if (nameList.length === 0) {
				if (await apply(data, editCode) === 0) {
					setInitLoader(false);
					setInputError([]);
				}
			} else {
				setInputError(inputError.concat(nameList));
			}
		}
	};

	const closeDisplay = () => {
		setInitLoader(false);
		setInputError([]);
		close();
	};

	const inputCheck = (value, maxLen, target) => {
		if (target === "logNo") {
			return /^[1-9]\d{0,2}$/.test(value);
		} else if (target === "description"){
			return stringBytes(value) <= maxLen;
		} else {
			return stringBytes(value) <= maxLen && value !== "";
		}
	}

	console.log("[CategoryModal] CategoryInfo",CategoryInfo);

	return (
		<>
			{isOpen ? (
				<ReactTransitionGroup
					transitionName={"Custom-modal-anim"}
					transitionEnterTimeout={200}
					transitionLeaveTimeout={200}
				>
					<div className="Custom-modal-overlay" onClick={() => closeDisplay()} />
					<div className="Custom-modal">
						<p className="title">Category Add/Edit</p>
						<div className="content-with-title system-setting" ref={allInput}>
							<FormGroup>
								<label className="title">No.</label>
								<Input type="text" id="cat_code" name="logNo" value={CategoryInfo.logNo} onChange={changeHandler} maxLength="3" disabled={isEdit !== 0} />
								<span className={"error" + (inputError.indexOf("logNo") !== -1 ? " active" : "")}>No. is invalid.</span>
								<ModalTooltip target="cat_code" header="No." body="Range: 1 ~ 999" placement="right" trigger="focus" />
							</FormGroup>
							<FormGroup>
								<label className="title">Category Name</label>
								<Input type="text" id="cat_name" name="logName" value={CategoryInfo.logName} onChange={changeHandler} maxLength="50" />
								<span className={"error" + (inputError.indexOf("logName") !== -1 ? " active" : "")}>Name is invalid.</span>
								<ModalTooltip target="cat_name" header="Name" body="You can input up to 50 byte." placement="right" trigger="focus" />
							</FormGroup>
							<FormGroup>
								<label className="title">File Path</label>
								<Input type="text" id="cat_path" name="filePath" value={CategoryInfo.filePath} onChange={changeHandler} maxLength="150" />
								<span className={"error" + (inputError.indexOf("filePath") !== -1 ? " active" : "")}>Path is invalid.</span>
								<ModalTooltip target="cat_path" header="Path" body="You can input up to 150 byte." placement="right" trigger="focus" />
							</FormGroup>
							<FormGroup>
								<label className="title">File Name</label>
								<Input type="text" id="cat_fname" name="fileName" value={CategoryInfo.fileName} onChange={changeHandler} maxLength="50" />
								<span className={"error" + (inputError.indexOf("fileName") !== -1 ? " active" : "")}>File Name is invalid.</span>
								<ModalTooltip target="cat_fname" header="File Name" body="You can input up to 50 byte." placement="right" trigger="focus" />
							</FormGroup>
							<FormGroup>
								<label className="title">Description</label>
								<Input type="text" id="cat_dsc" name="description" value={CategoryInfo.description} onChange={changeHandler} maxLength="50" />
								<span className={"error" + (inputError.indexOf("description") !== -1 ? " active" : "")}>Description is invalid.</span>
								<ModalTooltip target="cat_dsc" header="Description" body="You can input up to 50 byte." placement="right" trigger="focus" />
							</FormGroup>
							<FormGroup>
								<label className="title">Display</label>
								<div className="custom-check">
									<input
										type="checkbox"
										id="cat_display"
										name="display"
										checked={CategoryInfo.display === null ? false : CategoryInfo.display}
										onChange={changeToggleHandler}/>
									<label htmlFor="cat_display" />
								</div>
							</FormGroup>
							<FormGroup>
								<label className="title">Auto Download</label>
								<div className="custom-check" >
									<input
										type="checkbox"
										id="cat_auto"
										name="auto"
										checked={CategoryInfo.auto === null ? false : CategoryInfo.auto}
										onChange={changeToggleHandler}
										disabled={CategoryInfo.display === false}/>
									<label htmlFor="cat_auto"/>
								</div>
							</FormGroup>
							<FormGroup className="location">
								<label className="title location">Location</label>
								<Select
									id ="cat_dest"
									defaultValue={CategoryInfo.dest ? CategoryInfo.dest : "Cons"}
									onChange={(v) => {setCategoryInfo({...CategoryInfo, dest: v})}}>
									<Option className="admin-system" value="Cons" >Cons</Option>
									<Option className="admin-system" value="Logsv">Logsv</Option>
								</Select>
							</FormGroup>
						</div>
						<div className="button-wrap">
							<button className="form-type left-btn system-setting" onClick={() => applyFunc(CategoryInfo, isEdit)}>
								Apply
							</button>
							<button className="form-type right-btn system-setting" onClick={() => closeDisplay()}>
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

export default CategoryModal;