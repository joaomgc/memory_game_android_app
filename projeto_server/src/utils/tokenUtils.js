import jwt from 'jsonwebtoken';
import config from '../config.js';

/**
 * Generates a JWT token.
 * @param {Object} payload - Data to include in the token.
 * @param {Object} options - Additional JWT options (e.g., expiration time).
 * @returns {string} - The generated token.
 */
export const generateToken = (payload, options = { expiresIn: '1h' }) => {
  return jwt.sign(payload, config.jwtSecret, options);
};
