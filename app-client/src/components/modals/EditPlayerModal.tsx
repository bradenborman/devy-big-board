import React, { useState, useRef, useEffect } from 'react';
import ReactCrop, { Crop, PixelCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import { Player } from '../draft/BigBoard';
import './addPlayerModal.scss';

interface PlayerWithId extends Player {
    id?: number;
    verified?: boolean;
    imageUrl?: string;
}

interface EditPlayerModalProps {
    visible: boolean;
    player: PlayerWithId | null;
    onClose: () => void;
    onSubmit: (player: PlayerWithId, verificationCode: string) => void;
}

const EditPlayerModal: React.FC<EditPlayerModalProps> = ({ visible, player, onClose, onSubmit }) => {
    const [name, setName] = useState('');
    const [position, setPosition] = useState('');
    const [team, setTeam] = useState('');
    const [college, setCollege] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const currentYear = new Date().getFullYear();
    const [draftyear, setDraftyear] = useState<number>(currentYear);
    
    // Image upload states
    const [currentImageUrl, setCurrentImageUrl] = useState<string | null>(null);
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

    useEffect(() => {
        if (player) {
            setName(player.name);
            setPosition(player.position);
            setTeam(player.team || '');
            setCollege(player.college || '');
            setDraftyear(player.draftyear || currentYear);
            setCurrentImageUrl(player.imageUrl || null);
        }
    }, [player]);

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
        if (!name || !position || !verificationCode || !player?.id) return;

        const updatedPlayer: PlayerWithId = {
            ...player,
            name,
            position,
            team,
            college,
            draftyear
        };

        try {
            // Update player data first
            await onSubmit(updatedPlayer, verificationCode);

            // If there's a new cropped image, upload it
            if (croppedImageBlob && player.id) {
                const formData = new FormData();
                formData.append('file', croppedImageBlob, 'headshot.jpg');

                await fetch(`/api/players/manage/${player.id}/headshot`, {
                    method: 'POST',
                    body: formData
                });
            }

            resetForm();
        } catch (error) {
            console.error('Error updating player:', error);
        }
    };

    const resetForm = () => {
        setVerificationCode('');
        setSelectedImage(null);
        setCroppedImageBlob(null);
        setCompletedCrop(null);
    };

    const handleCropComplete = async (crop: PixelCrop) => {
        setCompletedCrop(crop);
        const blob = await getCroppedImg();
        setCroppedImageBlob(blob);
    };

    const handleClose = () => {
        resetForm();
        onClose();
    };

    if (!visible || !player) return null;

    const yearOptions = Array.from({ length: 6 }, (_, i) => currentYear + i);

    return (
        <div className="modal-overlay" onClick={handleClose}>
            <div className="modal-content add-player-modal" onClick={(e) => e.stopPropagation()}>
                <div className="modal-header">
                    <h2>Edit Player</h2>
                    <button className="close-btn" onClick={handleClose}>×</button>
                </div>

                <div className="modal-body">
                    {/* Image Upload Section */}
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
                                {currentImageUrl ? (
                                    <>
                                        <img 
                                            src={currentImageUrl} 
                                            alt={name}
                                            style={{ 
                                                maxWidth: '200px', 
                                                maxHeight: '200px',
                                                borderRadius: '8px',
                                                marginBottom: '1rem'
                                            }}
                                        />
                                        <p>Click to change headshot</p>
                                        {croppedImageBlob && (
                                            <div style={{ marginTop: '0.5rem', color: '#4CAF50' }}>
                                                ✓ New image ready to upload
                                            </div>
                                        )}
                                    </>
                                ) : (
                                    <>
                                        <div className="upload-icon">📷</div>
                                        <p>Click to upload headshot</p>
                                        <small>Recommended: Square image, min 200x200px</small>
                                        {croppedImageBlob && (
                                            <div style={{ marginTop: '1rem', color: '#4CAF50' }}>
                                                ✓ Image ready to upload
                                            </div>
                                        )}
                                    </>
                                )}
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
                                <div className="crop-actions">
                                    <button 
                                        type="button"
                                        className="btn-apply-crop"
                                        onClick={() => {
                                            if (croppedImageBlob) {
                                                setSelectedImage(null);
                                            }
                                        }}
                                        disabled={!croppedImageBlob}
                                    >
                                        ✓ Apply Crop
                                    </button>
                                    <button 
                                        type="button"
                                        className="btn-cancel-crop"
                                        onClick={() => {
                                            setSelectedImage(null);
                                            setCroppedImageBlob(null);
                                        }}
                                    >
                                        Cancel
                                    </button>
                                </div>
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
                        <label>Admin Verification Code *</label>
                        <input 
                            type="password"
                            placeholder="Required to update player" 
                            value={verificationCode} 
                            onChange={(e) => setVerificationCode(e.target.value)} 
                        />
                        <small className="help-text">
                            🔒 Verification code is required to save changes
                        </small>
                    </div>
                </div>

                <div className="modal-footer">
                    <button className="btn-cancel" onClick={handleClose}>Cancel</button>
                    <button 
                        className="btn-submit" 
                        onClick={handleSubmit} 
                        disabled={!name || !position || !verificationCode}
                    >
                        Update Player
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EditPlayerModal;
