import React, {Component, useEffect, useState} from "react";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheckCircle, faUserCircle} from "@fortawesome/free-solid-svg-icons";
import {
  Button,
  DropdownItem,
  DropdownMenu,
  DropdownToggle,
  FormGroup,
  Nav,
  Navbar,
  NavbarBrand,
  UncontrolledDropdown
} from "reactstrap";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {NavLink as RRNavLink} from "react-router-dom";
import {connect} from "react-redux";
import {bindActionCreators} from "redux";
import * as loginActions from "../../modules/login";
import * as API from "../../api";
import * as Define from "../../define";
import LogOutModal from "../User/LogOut";
import ChangePwModal from "../User/ChangePw";
import AlertModal from "../Common/AlertModal";
import services from "../../services"
import {faClock} from "@fortawesome/free-regular-svg-icons";

const PASSWORD_ALERT_MESSAGE = "Password change completed.";

class RSSNavbar extends Component{
  constructor(props) {
    super(props);
    this.state = {
      currentPage: "Manual",
      isPasswordOpen : false,
      isLogoutOpen : false,
      isAlertOpen: false,
      isPlanOpen: false,
      alertMessage: "",
    };
  }

  getClassName = page => {
    const { currentPage } = this.state;
    return currentPage === page ? "nav-item-custom-select" : null;
  };

  handlePageChange = async page => {
    this.setState({
      currentPage: page
    });

    await this.closeModal();
  };
  
  openModal = async (sMode) => {
    switch(sMode) {
      case "password":
        await this.setState({isPasswordOpen: true });
        break;

      case "logout":
        await this.setState({isLogoutOpen: true });
        break;

      case "plan":
        await this.setState({ isPlanOpen: true });
        break;

      default:
        console.log("sMode error:" + sMode);
        break;
    }
  }
  
  closeModal = async () => {
  	await this.setState(() => ({
      isPasswordOpen: false,
      isLogoutOpen: false,
      isPlanOpen: false
  	}));
  }

  openAlert = (type) => {
    setTimeout(() => {
      switch(type) {
        case "password":
          this.setState({
            isAlertOpen: true,
            alertMessage: PASSWORD_ALERT_MESSAGE
          });
          break;

        default:
          console.log("invalid type!!");
          break;
      }
    }, 200);
  }

  closeAlert = () => {
    this.setState({
      isAlertOpen: false,
      alertMessage: ""
    });
  }

  onLogout = async () => {
    await API.setLoginInit(this.props);
    try {
      await services.axiosAPI.requestGet(Define.REST_AUTHS_GET_LOGOUT);
    } catch (e) {
      console.error(e);
    }
    sessionStorage.setItem("accessToken", "");
    sessionStorage.setItem("refreshToken", "");
    this.props.onMovePage(Define.PAGE_LOGIN);
  };

  render() {
    const { isPasswordOpen, isLogoutOpen, isAlertOpen, isPlanOpen, alertMessage } = this.state;
    const permission = this.props.loginInfo.get("auth");

    return (
        <>
          <AlertModal isOpen={isAlertOpen} icon={faCheckCircle} message={alertMessage} style={"gray"} closer={this.closeAlert} />
          <ChangePwModal isOpen={isPasswordOpen} right={this.closeModal} alertOpen={this.openAlert}/>
          <LogOutModal isOpen={isLogoutOpen} left={this.onLogout} right={this.closeModal} />
          <PlanModal isOpen={isPlanOpen} btnAction={this.handlePageChange} closer={this.closeModal} />
          <div className="navbar-container">
            <Navbar color="dark" dark expand="md">
              <NavbarBrand
                  tag={RRNavLink}
                  to={Define.PAGE_REFRESH_DEFAULT}    // move to first initialized ftp manual page
                  className="custom-brand"
                  onClick={() => this.handlePageChange("Manual")}
              >
                {"Rapid Collector"}
              </NavbarBrand>
            <Nav className="mr-auto" navbar>
              <UncontrolledDropdown nav inNavbar className={this.getClassName("Manual")}>
                <DropdownToggle nav>Manual Download</DropdownToggle>
                <DropdownMenu>
                    <DropdownItem tag={RRNavLink} to={Define.PAGE_REFRESH_MANUAL_FTP} onClick={() => this.handlePageChange("Manual")}>
                        FTP
                    </DropdownItem>
                    { permission.manual_vftp &&
                      <>
                        <DropdownItem divider />
                        <DropdownItem tag={RRNavLink} to={Define.PAGE_REFRESH_MANUAL_VFTP_COMPAT} onClick={() => this.handlePageChange("Manual")}>
                        VFTP(COMPAT)
                        </DropdownItem>
                        <DropdownItem divider />
                        <DropdownItem tag={RRNavLink} to={Define.PAGE_REFRESH_MANUAL_VFTP_SSS} onClick={() => this.handlePageChange("Manual")}>
                        VFTP(SSS)
                        </DropdownItem>
                      </>
                    }
                </DropdownMenu>
              </UncontrolledDropdown>
              { permission.auto &&
                <UncontrolledDropdown nav inNavbar>
                  <DropdownToggle nav className={this.getClassName("Auto")}>
                    Auto Download
                  </DropdownToggle>
                  <DropdownMenu>
                    <DropdownItem onClick={() => this.openModal("plan")}>
                      Add New Plan
                    </DropdownItem>
                    <DropdownItem divider/>
                    <DropdownItem
                        tag={RRNavLink}
                        to={Define.PAGE_REFRESH_AUTO_STATUS}
                        onClick={() => this.handlePageChange("Auto")}
                    >
                      Plan Status
                    </DropdownItem>
                  </DropdownMenu>
                </UncontrolledDropdown>
              }
              { (permission.system_log || permission.system_restart || permission.account || permission.config) &&
              <UncontrolledDropdown nav inNavbar className={this.getClassName("admin")}>
                <DropdownToggle nav tag={RRNavLink} to={Define.PAGE_REFRESH_ADMIN}
                                onClick={() => this.handlePageChange("admin")}>
                  Administrator
                </DropdownToggle>
              </UncontrolledDropdown>
              }
              </Nav>
              <Nav className="ml-auto" navbar>
                <ServerTime />
                <UncontrolledDropdown nav inNavbar>
                  <DropdownToggle nav>
                    <FontAwesomeIcon icon={faUserCircle} size="lg"/> {API.getLoginUserName(this.props)}
                  </DropdownToggle>
                  <DropdownMenu right>
                    <DropdownItem onClick={() => this.openModal("password")}>Change Password</DropdownItem>
                    <DropdownItem divider/>
                    <DropdownItem onClick={() => this.openModal("logout")}>Logout</DropdownItem>
                  </DropdownMenu>
                </UncontrolledDropdown>
              </Nav>
            </Navbar>
          </div>
        </>
    );
  }
}

const PlanModal = ({ isOpen, btnAction, closer }) => {
  return (
      <>
        {isOpen ? (
            <ReactTransitionGroup
                transitionName={"Custom-modal-anim"}
                transitionEnterTimeout={200}
                transitionLeaveTimeout={200}
            >
              <div className="Custom-modal-overlay" />
              <div className="Custom-modal">
                <p className="title">New Plan</p>
                <div className="content-with-title add-plan-modal">
                  <FormGroup className="plan-btn-area">
                    <Button
                        color="info"
                        outline
                        block
                        tag={RRNavLink}
                        to={`${Define.PAGE_REFRESH_AUTO_PLAN_ADD}&type=${Define.PLAN_TYPE_FTP}`}
                        onClick={() => btnAction("Auto")}
                    >
                      FTP
                    </Button>
                    <Button
                        color="info"
                        outline
                        block
                        tag={RRNavLink}
                        to={`${Define.PAGE_REFRESH_AUTO_PLAN_ADD}&type=${Define.PLAN_TYPE_VFTP_COMPAT}`}
                        onClick={() => btnAction("Auto")}
                    >
                      VFTP(COMPAT)
                    </Button>
                    <Button
                        color="info"
                        outline
                        block
                        tag={RRNavLink}
                        to={`${Define.PAGE_REFRESH_AUTO_PLAN_ADD}&type=${Define.PLAN_TYPE_VFTP_SSS}`}
                        onClick={() => btnAction("Auto")}
                    >
                      VFTP(SSS)
                    </Button>
                  </FormGroup>
                </div>
                <div className="button-wrap">
                  <button className="gray alert-type" onClick={closer}>
                    Close
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
};

export default connect(
    (state) => ({
      loginInfo : state.login.get('loginInfo'),
    }),
    (dispatch) => ({
      loginActions: bindActionCreators(loginActions, dispatch),
    })
)(RSSNavbar);

function ServerTime() {
  const [time, setTime] = useState("Unknown");

  const getServerTime = () => {
    fetch(Define.REST_INFOS_GET_SERVER_TIME)
      .then(res => {
        return res.json();
      })
      .then(json =>{
        setTime(json?.time || "Unknown")
      })
      .catch(error => {
        setTime("Unknown")
      });
  };

  useEffect(() => {
    getServerTime();
    setInterval(() => {
      getServerTime();
    }, 1000 * 60)
  }, [])

  return (
      <div style={{ color: "white", lineHeight: "40px", display: "flex", alignItems: "center" }}>
        <FontAwesomeIcon icon={faClock} />
        <div style={{ marginLeft : "5px" }}>{time}</div>
      </div>
  );
}