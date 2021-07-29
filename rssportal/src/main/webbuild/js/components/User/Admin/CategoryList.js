import React, {useState} from "react";
import {Row, Col, Card, CardHeader, CardBody, Input} from "reactstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faStream, faPlus, faSearch, faPen, faTrash, faExclamationCircle} from "@fortawesome/free-solid-svg-icons";
import CategoryModal from "./CategoryModal";
import AlertModal from "../../Common/AlertModal";
import {propsCompare} from "../../Common/CommonFunction";
import {connect} from "react-redux";
import services from "../../../services";
import * as Define from "../../../define";
import {bindActionCreators} from "redux";
import * as viewListActions from "../../../modules/viewList";

const msgList = {
	MSG_1: "This category has being collected.",
	MSG_2: "The No. is already registered. Please register again."
};

const CategoryList = React.memo(({ openDelete, logInfoList, logInfoVer ,viewListActions, loginInfo}) => {
	const [categoryModalOpen, setCategoryModalOpen] = useState(false);
	const [alertOpen, setAlertOpen] = useState(false);
	const [alertMsg, setAlertMsg] = useState("");
	const [onSearch, setOnSearch] = useState(false);
	const [query, setQuery] = useState("");
	const [selectIndex, setSelectIndex] = useState(0);
	const category = logInfoList.toJS();
	const permission = loginInfo.get("auth");

	const isCollectingPlan = async (Code) => {
		const res = await services.axiosAPI.requestGet(Define.REST_PLANS_GET_PLANS);
		const { lists } = res.data;
		return lists.find(plan => plan.categoryCodes.indexOf(Code) !== -1 && plan.detailedStatus !== "completed")
	}

	const updateCategory = async (flag, Code) => {
		if (flag === true) {
			const res = await isCollectingPlan(Code);
			console.log("res ", res);
			if (res === undefined) {
				setSelectIndex(Code);
				setCategoryModalOpen(flag);
			} else {
				openAlertModal(msgList.MSG_1);
			}
		} else {
			setSelectIndex(0);
			setCategoryModalOpen(flag);
		}
		console.log("Code :", Code);
	}

	const deleteCategory = async (flag, Code) => {
		const res = await isCollectingPlan(Code);
		console.log("[checkDeleteModal]res", res);
		if (res === undefined) {
			openDelete(flag, Code);
		} else {
			openAlertModal(msgList.MSG_1);
		}
	}

	const applyCategory = async (data, edit) => {
		console.log("============Apply Category=============");
		let result = 0;
		let msg = "";

		if (edit) {
			const isCollecting = await isCollectingPlan(data.logCode);
			if (isCollecting === undefined) {
				await services.axiosAPI.requestPatch(Define.REST_SYSTEM_SET_CATEGORIES, data);
			} else {
				msg = msgList.MSG_1;
			}
		} else {
			const res = await viewListActions.viewLoadLogTypeList(Define.REST_SYSTEM_GET_CATEGORIES);
			const isDuplicate = res.data.lists.find(obj => obj.categoryCode === data.logCode);
			if (isDuplicate === undefined) {
				await services.axiosAPI.requestPost(Define.REST_SYSTEM_SET_CATEGORIES, data);
			} else {
				msg = msgList.MSG_2;
				result = -1;
			}
		}

		if (result === 0) {
			await updateCategory(false, 0);
			await viewListActions.viewLoadLogTypeList(Define.REST_SYSTEM_GET_CATEGORIES);
		} else {
			setCategoryModalOpen(false);
			setTimeout(() => { openAlertModal(msg); }, 400);
		}
		console.log("==========================================");
		return result;
	}

	const openAlertModal = (msg) => {
		setAlertMsg(msg);
		setAlertOpen(true);
	}

	const closeAlertModal = () => {
		setAlertOpen(false);
		setAlertMsg("");

		if (alertMsg === msgList.MSG_2) {
			setTimeout(() => { setCategoryModalOpen(true); }, 400);
		}
	};

	return (
		<>
			<CategoryModal
				isOpen={categoryModalOpen}
				close={() => updateCategory(false, 0)}
				apply={(data,selectIndex) => applyCategory(data, selectIndex)}
				data={category}
				isEdit={selectIndex}
			/>
			<AlertModal
				isOpen={alertOpen}
				icon={faExclamationCircle}
				message={alertMsg}
				style="system-setting"
				closer={closeAlertModal}
			/>
			<Row className="pd-0 mt-3">
				<Col>
					<Card className="admin-system category">
						<CardHeader>
							<p>
								<span className="title-icon">
									<FontAwesomeIcon icon={faStream}/>
								</span>
								Category List
								<span className="title-version">
									(Version: {logInfoVer})
								</span>
							</p>
							{ permission.config &&
								<div className="func-section">
									<div>
										<input
											type="checkbox"
											id="search-toggle"
											checked={onSearch}
											onChange={() => {
												if (onSearch) {
													setQuery("");
												}
												setOnSearch(!onSearch);
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
											placeholder="Enter the category name to search."
											value={query}
											onChange={(e) => setQuery(e.target.value)}
										/>
									</div>
									<div>
										<button onClick={() => updateCategory(true, 0)}>
											<div className="bg" />
											<div className="text">
												<FontAwesomeIcon icon={faPlus} />
											</div>
										</button>
									</div>
								</div>
							}
						</CardHeader>
						<CardBody>
							{ permission.config
								?
								<>
									<div className="cat-table-header">
										<table>
											<thead>
											<tr>
												<th>No.</th>
												<th className="medium-size">Category Name</th>
												<th className="max-size">File Path</th>
												<th className="min-size">File Name</th>
												<th className="min-size">Location</th>
												<th className="min-size">Auto Download</th>
												<th className="min-size">Display</th>
												<th>Edit</th>
												<th>Delete</th>
											</tr>
											</thead>
										</table>
									</div>
									<CategoryFilter
										category={category}
										query={query}
										openEdit={updateCategory}
										openDelete={deleteCategory}
									/>
								</>
								:
								<div className={"category-no-permission"}>
									<p><FontAwesomeIcon icon={faExclamationCircle} size={"8x"}/></p>
									<p>You don't have permission.</p>
								</div>
							}
						</CardBody>
					</Card>
				</Col>
			</Row>
		</>
	);
});

const CategoryFilter = React.memo(({ category, query, openEdit, openDelete }) => {
	if (category.length === 0) {
		return (
			<div className="cat-table-body no-category">
				<p><FontAwesomeIcon icon={faExclamationCircle} size="8x"/></p>
				<p>No Registered Categories.</p>
			</div>
		);
	} else {
		const newCategory = query.length > 0 ? category.filter(data => data.logName.toLowerCase().includes(query.toLowerCase())) : category;

		return (
			newCategory.length === 0 ? (
				<div className="cat-table-body no-category">
					<p><FontAwesomeIcon icon={faExclamationCircle} size="9x"/></p>
					<p>Category not found.</p>
				</div>
			) : (
				<div className="cat-table-body">
					<table>
						<tbody>
						{newCategory.map((data, key) => {
							return (
								<tr key={key} title={data.description}>
									<td>{parseInt(data.logCode,16)}</td>
									<td className="medium-size">{data.logName}</td>
									<td className="max-size">{data.filePath}</td>
									<td className="min-size">{data.fileName}</td>
									<td className="min-size">{data.dest}</td>
									<td className="min-size">
										<span className={"round-badge " + (data.auto ? "green" : "red")}>
											{data.auto ? "true" : "false"}
										</span>
									</td>
									<td className="min-size">
										<span className={"round-badge " + (data.display ? "green" : "red")}>
											{data.display ? "true" : "false"}
										</span>
									</td>
									<td>
										<span className="action" onClick={() => openEdit(true, data.logCode)}>
											<FontAwesomeIcon icon={faPen}/>
										</span>
									</td>
									<td>
										<span className="action" onClick={() => openDelete("CATEGORY", data.logCode)}>
											<FontAwesomeIcon icon={faTrash}/>
										</span>
									</td>
								</tr>
							);
						})}
						</tbody>
					</table>
				</div>
			)
		);
	}
}, propsCompare);

export default connect(
	(state) => ({
		logInfoList: state.viewList.get('logInfoList'),
		logInfoVer: state.viewList.get('logInfoVer'),
		loginInfo : state.login.get('loginInfo'),
	}),
	(dispatch) => ({
		viewListActions: bindActionCreators(viewListActions, dispatch),
	})
)(CategoryList);