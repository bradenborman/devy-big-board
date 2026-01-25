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

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                <h2>Add Player</h2>
                <input placeholder="Name *" value={name} onChange={(e) => setName(e.target.value)} />

                <select value={position} onChange={(e) => setPosition(e.target.value)}>
                    <option value="">Select Position *</option>
                    <option value="QB">QB</option>
                    <option value="RB">RB</option>
                    <option value="WR">WR</option>
                    <option value="TE">TE</option>
                </select>

                <input placeholder="Team" value={team} onChange={(e) => setTeam(e.target.value)} />
                
                <input placeholder="College" value={college} onChange={(e) => setCollege(e.target.value)} />

                <label className="slider-label">
                    Draft Year: <strong>{draftyear}</strong>
                </label>
                <input
                    type="range"
                    min={currentYear}
                    max={currentYear + 5}
                    value={draftyear}
                    onChange={(e) => setDraftyear(Number(e.target.value))}
                />

                <div className="verification-section">
                    <input 
                        type="password"
                        placeholder="Verification Code (optional)" 
                        value={verificationCode} 
                        onChange={(e) => setVerificationCode(e.target.value)} 
                    />
                    <small className="help-text">
                        Leave blank to submit as pending. Admins can verify later.
                    </small>
                </div>

                <button onClick={handleSubmit}>Add Player</button>
                <button className="cancel" onClick={onClose}>Cancel</button>
            </div>
        </div>
    );
};

export default AddPlayerModal;
