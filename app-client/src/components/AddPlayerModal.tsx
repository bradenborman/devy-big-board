import React, { useState } from 'react';
import { Player } from './bigBoard';
import './addPlayerModal.scss';

interface AddPlayerModalProps {
    visible: boolean;
    onClose: () => void;
    onSubmit: (player: Player, verificationCode?: string) => void;
}

const AddPlayerModal: React.FC<AddPlayerModalProps> = ({ visible, onClose, onSubmit }) => {
    const [name, setName] = useState('');
    const [position, setPosition] = useState('');
    const [team, setTeam] = useState('');
    const [college, setCollege] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const currentYear = new Date().getFullYear();
    const [draftyear, setDraftyear] = useState<number>(currentYear);

    const handleSubmit = () => {
        if (!name || !position) return;

        onSubmit({
            name,
            position,
            team,
            college,
            draftyear,
            adp: -1
        }, verificationCode || undefined);

        setName('');
        setPosition('');
        setTeam('');
        setCollege('');
        setVerificationCode('');
        setDraftyear(currentYear);
        onClose();
    };

    if (!visible) return null;

    // Generate year options (current year + next 5 years)
    const yearOptions = Array.from({ length: 6 }, (_, i) => currentYear + i);

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content add-player-modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Add New Player</h2>
                    <button className="close-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    <div className="form-group">
                        <label>Player Name *</label>
                        <input 
                            placeholder="e.g., Caleb Williams" 
                            value={name} 
                            onChange={(e) => setName(e.target.value)} 
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label>Position *</label>
                            <select value={position} onChange={(e) => setPosition(e.target.value)}>
                                <option value="">Select</option>
                                <option value="QB">QB</option>
                                <option value="RB">RB</option>
                                <option value="WR">WR</option>
                                <option value="TE">TE</option>
                            </select>
                        </div>

                        <div className="form-group">
                            <label>Draft Year *</label>
                            <select value={draftyear} onChange={(e) => setDraftyear(Number(e.target.value))}>
                                {yearOptions.map(year => (
                                    <option key={year} value={year}>{year}</option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label>NFL Team</label>
                        <input 
                            placeholder="e.g., Chicago Bears" 
                            value={team} 
                            onChange={(e) => setTeam(e.target.value)} 
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>College</label>
                        <input 
                            placeholder="e.g., USC" 
                            value={college} 
                            onChange={(e) => setCollege(e.target.value)} 
                        />
                    </div>

                    <div className="form-group verification-group">
                        <label>Admin Verification Code</label>
                        <input 
                            type="password"
                            placeholder="Optional - leave blank for pending status" 
                            value={verificationCode} 
                            onChange={(e) => setVerificationCode(e.target.value)} 
                        />
                        <small className="help-text">
                            💡 Only admins have this code. Players without it will be marked as pending.
                        </small>
                    </div>
                </div>

                <div className="modal-footer">
                    <button className="btn-cancel" onClick={onClose}>Cancel</button>
                    <button className="btn-submit" onClick={handleSubmit} disabled={!name || !position}>
                        Add Player
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AddPlayerModal;
