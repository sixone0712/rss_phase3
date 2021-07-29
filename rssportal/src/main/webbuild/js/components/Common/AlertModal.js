import React from "react";
import ReactTransitionGroup from "react-addons-css-transition-group";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const AlertModal = React.memo(({isOpen, icon, message, style, closer}) => {
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
                    <div className="content-without-title">
                        <p><FontAwesomeIcon icon={icon} size="8x" /></p>
                        <p>{message}</p>
                    </div>
                    <div className="button-wrap">
                        <button className={"alert-type " + style} onClick={closer}>Close</button>
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
});

export default AlertModal;
