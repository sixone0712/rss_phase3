import React, {Component} from "react";
import {Breadcrumb, BreadcrumbItem, Button, Card, CardBody, CardHeader, Col, Container, Table} from "reactstrap";
import * as API from "../../api";
import * as Define from "../../define";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as dlHistoryAction from "../../modules/dlHistory";
import {Select} from "antd";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faAngleDoubleUp, faExclamationCircle, faCloudDownloadAlt, faSyncAlt} from "@fortawesome/free-solid-svg-icons";
import FadeLoader from "react-spinners/FadeLoader";
import {filePaginate, RenderPagination} from "../Common/CommonFunction";
import Footer from "../Common/Footer";
import ScrollToTop from "react-scroll-up";
import moment from "moment";
import {CSVLink} from "react-csv";
import {Link as RouterLink} from "react-router-dom";

const { Option } = Select;
const formatDate = 'YYYY/MM/DD HH:mm:ss';

export function getDownloadType(type) {
    let typeString = 0;
    //console.log("type: ",type);
    typeString = (type == Define.RSS_TYPE_FTP_MANUAL)  ? "Manual download(FTP)"
        : (type == Define.RSS_TYPE_FTP_AUTO)  ? "Auto download(FTP)"
        : (type == Define.RSS_TYPE_VFTP_MANUAL_SSS)  ? "Manual download(VFTP/SSS)"
        : (type == Define.RSS_TYPE_VFTP_MANUAL_COMPAT)  ? "Manual download(VFTP/COMPAT)"
        : (type == Define.RSS_TYPE_VFTP_AUTO_COMPAT)  ? "Auto download(VFTP/COMPAT)"
        : (type == Define.RSS_TYPE_VFTP_AUTO_SSS)  ? "Auto download(VFTP/SSS)"
        : " ";
    return typeString;
}
const scrollStyle = {
    backgroundColor: "#343a40",
    width: "40px",
    height: "40px",
    textAlign: "center",
    borderRadius: "3px",
    zIndex: "101",
    bottom: "70px"
};
class DownloadHistory extends Component {
    constructor(props) {
        super(props);
        this.state = {
            pageSize: 10,
            currentPage: 1,
            data:[],
        };

    }
    loadHistory =()=>{
        const loadHistory = async () => {
            return await API.loadDlHistoryList(this.props);
        }
        loadHistory().then(r => r).catch(e => console.log(e));
    };

    downloadHistory =(historyData)=>{
        const filteredData = historyData.map((item) => {
            return (
                {
                    "No.": item.dl_Idx,
                    "User Name": item.dl_user,
                    "Downloaded Date": moment(item.dl_date, "YYYYMMDDHHmmss").format(formatDate),
                    "Downloaded File name": item.dl_filename,
                    "Downloaded Type": getDownloadType(item.dl_type),
                    "Status": item.dl_status
                }
            );
        });
        this.setState({ ...this.state, data: filteredData })
    };

    componentDidMount() {
        this.loadHistory();
    };

    handlePaginationChange = page => {
        this.setState({
            ...this.state,
            currentPage: page
        });
    };

    handleSelectBoxChange = value => {
        const { pageSize, currentPage } = this.state;
        const startIndex = (currentPage - 1) * pageSize === 0 ? 1 : (currentPage - 1) * pageSize + 1;

        this.setState({
            pageSize: value,
            currentPage: Math.ceil(startIndex / value)
        });
    };

     render() {
        const {currentPage, pageSize} = this.state;
        const historyList = API.getDlHistoryList(this.props);
        const isInit = API.getDlHistoryTotalCnt(this.props);
        const lists = filePaginate(historyList, currentPage, pageSize);
        const { length: count } = historyList;
        let renderComponent;

        if (isInit === -1) {
            renderComponent =
                <div className="page-loader-area download-history">
                    <FadeLoader height={30} width={10} radius={2} margin={20} color={"#17a2b8"} />
                </div>;
        } else if (count === 0) {
            renderComponent =
                <Card className="auto-plan-box">
                    <CardHeader className="auto-plan-card-header">
                        Download History
                        <p>Check the <span>user's download history.</span></p>
                    </CardHeader>
                    <CardBody className="auto-plan-card-body">
                        <Col className="auto-plan-collection-list">
                            <p className="no-registered-plan icon">
                                <FontAwesomeIcon icon={faExclamationCircle} size="7x" />
                            </p>
                            <p className="no-registered-plan message">
                                No registered user's download history.
                            </p>
                        </Col>
                    </CardBody>
                </Card>;
        } else {
            renderComponent =
                <Card className="auto-plan-box administrator">
                    <CardHeader className="auto-plan-card-header administrator">
                        Download History
                        <p>Check the <span>user's download history.</span></p>
                        <div className="select-area">
                            <label>Rows per page : </label>
                            <Select defaultValue={10} onChange={this.handleSelectBoxChange} className="administrator">
                                <Option value={10}>10</Option>
                                <Option value={30}>30</Option>
                                <Option value={50}>50</Option>
                                <Option value={100}>100</Option>
                            </Select>
                        </div>
                    </CardHeader>
                    <CardBody className="auto-plan-card-body not-flex">
                        <div className="auto-plan-collection-list plan-list">
                            <div className="content-section header ">
                                <div className="info-area administrator">Registered download history: {count}</div>
                                <div className="btn-area administrator">
                                    <CSVLink data={this.state.data}
                                             filename={'downloadHistory.csv'}
                                             target="_blank"
                                    >
                                        <Button size="sm" className="download-btn " onClick={()=>this.downloadHistory(historyList)}>
                                            <FontAwesomeIcon icon={faCloudDownloadAlt}/>
                                        </Button>
                                        <div className="custom-btn-tooltip">Download CSV</div>
                                    </CSVLink>
                                     <Button size="sm" className="download-btn" onClick={this.loadHistory}>
                                        <FontAwesomeIcon icon={faSyncAlt}/>
                                    </Button>
                                    <div className="custom-btn-tooltip">Update History</div>
                                </div>
                            </div>
                            <Table>
                                <thead>
                                <tr>
                                    <th>No.</th>
                                    <th>User Name</th>
                                    <th>Downloaded Date</th>
                                    <th>Downloaded File name</th>
                                    <th>Downloaded Type</th>
                                    <th>Status</th>
                                </tr>
                                </thead>
                                <tbody>
                                {lists.map((history, index) => {
                                    return (
                                        <tr key={index}>
                                            <td>{history.dl_Idx}</td>
                                            <td>{history.dl_user}</td>
                                            <td>{(history.dl_date != null) ? moment(history.dl_date, "YYYYMMDDHHmmss").format(formatDate) : ""}</td>
                                            <td>{history.dl_filename}</td>
                                            <td>{getDownloadType(history.dl_type)}</td>
                                            <td>{history.dl_status}</td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </Table>
                        </div>
                    </CardBody>
                    <RenderPagination
                        pageSize={pageSize}
                        itemsCount={count}
                        onPageChange={this.handlePaginationChange}
                        currentPage={currentPage}
                        className={"custom-pagination"}
                    />
                </Card>;
        }

        return (
            <>
                <Container className="rss-container" fluid={true}>
                    <Breadcrumb className="topic-path">
                        <BreadcrumbItem>
	                        <RouterLink to={Define.PAGE_REFRESH_ADMIN} className="link">
	                          Administrator
	                        </RouterLink>
                        </BreadcrumbItem>
                        <BreadcrumbItem active>Download History</BreadcrumbItem>
                    </Breadcrumb>
                    {renderComponent}
                </Container>
                <Footer/>
                <ScrollToTop showUnder={160} style={scrollStyle}>
                    <span className="scroll-up-icon"><FontAwesomeIcon icon={faAngleDoubleUp} size="lg"/></span>
                </ScrollToTop>
            </>
        );
    }
}

export default connect(
    (state) => ({
        dlHistoryInfo: state.dlHistory.get('dlHistoryInfo'),
    }),
    (dispatch) => ({
        dlHistoryAction: bindActionCreators(dlHistoryAction, dispatch),
    })
)(DownloadHistory);