import React, { useState, useRef } from 'react';
import ReactCrop, { Crop, PixelCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
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
    
    // Image upload states
    const [selectedImage, setSelectedImage] = useState<string | null>(null);
    const [crop, setCrop] = useState<Crop>({
        unit: '%',
        width: 50,
        height: 50,
        x: 25,
        y: 25
    });
    const [completedCrop, setCompletedCrop] = useState<PixelCrop | null>(null);
    const imgRef = useRef<HTMLImageElement>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [croppedImageBlob, setCroppedImageBlob] = useState<Blob | null>(null);

    const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const reader = new FileReader();
            reader.addEventListener('load', () => {
                setSelectedImage(reader.result?.toString() || null);
            });
            reader.readAsDataURL(e.target.files[0]);
        }
    };

    const getCroppedImg = async (): Promise<Blob | null> => {
        if (!completedCrop || !imgRef.current) return null;

        const image = imgRef.current;
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        if (!ctx) return null;

        const scaleX = image.naturalWidth / image.width;
        const scaleY = image.naturalHeight / image.height;

        canvas.width = completedCrop.width;
        canvas.height = completedCrop.height;

        ctx.drawImage(
            image,
            completedCrop.x * scaleX,
            completedCrop.y * scaleY,
            completedCrop.width * scaleX,
            completedCrop.height * scaleY,
            0,
            0,
            completedCrop.width,
            completedCrop.height
        );

        return new Promise((resolve) => {
            canvas.toBlob((blob) => {
                resolve(blob);
            }, 'image/jpeg', 0.95);
        });
    };

    const handleSubmit = async () => {
        if (!name || !position) return;

        const newPlayer: Player = {
            name,
            position,
            team,
            college,
            draftyear,
            adp: -1
        };

        try {
            const response = await fetch('/api/players/manage', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    ...newPlayer,
                    verificationCode: verificationCode || undefined
                })
            });

            if (!response.ok) {
                throw new Error('Failed to create player');
            }

            const createdPlayer = await response.json();

            if (croppedImageBlob && createdPlayer.id) {
                const formData = new FormData();
                formData.append('file', croppedImageBlob, 'headshot.jpg');

                await fetch(`/api/players/manage/${createdPlayer.id}/headshot`, {
                    method: 'POST',
                    body: formData
                });
            }

            onSubmit(newPlayer, verificationCode || undefined);
            resetForm();
            onClose();
        } catch (error) {
            console.error('Error creating player:', error);
            alert('Failed to create player. Please try again.');
        }
    };

    const resetForm = () => {
        setName('');
        setPosition('');
        setTeam('');
        setCollege('');
        setVerificationCode('');
        setDraftyear(currentYear);
        setSelectedImage(null);
        setCroppedImageBlob(null);
        setCompletedCrop(null);
    };

    const handleCropComplete = async (crop: PixelCrop) => {
        setCompletedCrop(crop);
        const blob = await getCroppedImg();
        setCroppedImageBlob(blob);
    };

    if (!visible) return null;

    const yearOptions = Array.from({ length: 6 }, (_, i) => currentYear + i);

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content add-player-modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Add New Player</h2>
                    <button className="close-btn" onClick={onClose}>×</button>
                </div>

                <div className="modal-body">
                    <div className="form-group image-upload-section">
                        <label>Player Headshot</label>
                        <input
                            ref={fileInputRef}
                            type="file"
                            accept="image/*"
                            onChange={handleImageSelect}
                            style={{ display: 'none' }}
                        />
                        
                        {!selectedImage ? (
                            <div 
                                className="image-upload-placeholder"
                                onClick={() => fileInputRef.current?.click()}
                            >
                                <div className="upload-icon">📷</div>
                                <p>Click to upload headshot</p>
                                <small>Recommended: Square image, min 200x200px</small>
                            </div>
                        ) : (
                            <div className="image-crop-container">
                                <ReactCrop
                                    crop={crop}
                                    onChange={(c) => setCrop(c)}
                                    onComplete={handleCropComplete}
                                    aspect={1}
                                >
                                    <img
                                        ref={imgRef}
                                        src={selectedImage}
                                        alt="Crop preview"
                                        style={{ maxWidth: '100%' }}
                                    />
                                </ReactCrop>
                                <button 
                                    type="button"
                                    className="btn-change-image"
                                    onClick={() => fileInputRef.current?.click()}
                                >
                                    Change Image
                                </button>
                            </div>
                        )}
                    </div>

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
