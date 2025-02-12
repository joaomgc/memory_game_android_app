import jwt from 'jsonwebtoken';
import config from '../config.js';

export const withAuth = (req, res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader) {
    return res.status(403).json({ message: "The authorization header is empty!" });
  }

  const token = authHeader.split(' ')[1]; // Extract token from "Bearer <token>"
  jwt.verify(token, config.jwtSecret, (err, decoded) => {
    if (err) {
      return res.status(401).json({ message: "Unauthorized!" });
    }

    req.user = decoded; // Attach decoded token payload to the request
    next();
  });
};

// Middleware to check roles (optional, for specific permissions)
export const withRole = (roles) => (req, res, next) => {
  if (!roles.includes(req.user.role)) {
    return res.status(403).json({ message: "Insufficient permissions" });
  }
  next();
};
